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
	definition (name: "Aeon DSD37 Repeater", namespace: "fuzzysb", author: "Stuart Buchanan") {
		capability "Actuator"
		capability "Refresh"
		capability "Configuration"
		
		/* Capability notes
		0x20 COMMAND_CLASS_BASIC 1
		0x70 COMMAND_CLASS_CONFIGURATION 1
		0x72 COMMAND_CLASS_MANUFACTURER_SPECIFIC 2
		0x85 COMMAND_CLASS_ASSOCIATION 2
		0x73 COMMAND_CLASS_POWERLEVEL 1
		0x86 COMMAND_CLASS_VERSION 2
		*/

        fingerprint deviceId: "0x0F01", inClusters: "0x20 0x70 0x72 0x85 0x73 0x86"
	}

	// simulator metadata
	simulator {
	}

	// tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","refresh"])
	}
}
def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x70: 1, 0x72: 2, 0x85: 2, 0x73: 1, 0x86: 1])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	createEvent([
		name: "switch", value: cmd.value ? "on" : "off", type: "physical"
	])
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "No handler for $cmd"
	// Handles all Z-Wave commands we aren't interested in
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}


def poll() {
	zwave.basicV1.basicGet().format()
}

def refresh() {
	zwave.basicV1.basicGet().format()
}

def configure() {
    log.debug "configure()"
}

def updated() {

	refresh()
}
