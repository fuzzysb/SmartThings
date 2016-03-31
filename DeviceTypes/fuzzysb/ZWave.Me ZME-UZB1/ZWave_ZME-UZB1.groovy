/**
 *  Copyright 2016 Stuart Buchanan
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "ZWave.Me Z-Wave Controller", namespace: "fuzzysb", author: "Stuart Buchanan") {

		command "on"
		command "off"
		
		/* Capability notes
        0x21 COMMAND_CLASS_CONTROLLER_REPLICATION 1
        0x20 COMMAND_CLASS_BASIC 1
        0x86 COMMAND_CLASS_VERSION 1
        0x98 COMMAND_CLASS_SECURITY 1
		*/

		fingerprint deviceId: "0x0201", inClusters: "0x21 0x20 0x86 0x98"
	}

	simulator {

	}

	tiles {
		standardTile("state", "device.state", width: 2, height: 2) {
			state 'connected', icon: "st.unknown.zwave.static-controller", backgroundColor:"#ffffff"
		}
		standardTile("basicOn", "device.switch", inactiveLabel:false, decoration:"flat") {
			state "on", label:"on", action:"on", icon:"st.switches.switch.on"
		}
		standardTile("basicOff", "device.switch", inactiveLabel: false, decoration:"flat") {
			state "off", label:"off", action:"off", icon:"st.switches.switch.off"
		}

		main "state"
		details(["state", "basicOn", "basicOff"])
	}
}


def parse(String description)
{
	def result = null
	if (description.startsWith("Err 106")) {
		state.sec = 0
		result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
			descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x98: 1, 0x86: 1, 0x72: 2, 0x20: 1, 0x21: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
            log.debug "Parse returned ${result?.inspect()}"
            		} else {
			log.debug("Couldn't zwave.parse '$description'")
			null
		}
		
	}
	log.debug "Parsed '${description}' to ${result.inspect()}"
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x86: 1, 0x72: 2, 0x20: 1, 0x21: 1])
	state.sec = 1
	log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	def event = [isStateChange: true]
	event.linkText = device.label ?: device.name
	event.descriptionText = "$event.linkText: $cmd"
	event
}


def on() {
	zwave.basicV1.basicSet(value: 0xFF).format()
}

def off() {
	zwave.basicV1.basicSet(value: 0x00).format()
}

private secure(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
         log.debug "Sending Secure Command $cmd"
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
    	log.debug "Sending Insecure Command $cmd"
		cmd.format()
	}
}
