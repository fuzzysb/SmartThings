/**
 *  Device Type Definition File
 *
 *  Device Type:		Fibaro Universal Sensor - Dual Contact Sensor
 *  File Name:			Fibaro Universal Sensor - Dual Contact Sensor.groovy
 *	Initial Release:	2016-01-17
 *	Author:				Stuart Buchanan
 *
 *  Copyright 2016 Stuart Buchanan, based on original code by carlos.ir33 with thanks
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
 ***************************************************************************************
 */
 
metadata {
	definition (name: "Fibaro Universal Sensor - Dual Contact", namespace: "fuzzysb", author: "Stuart Buchanan") {
    capability 	"Contact Sensor"
    capability 	"Configuration"
    command "listCurrentParams"
	
	fingerprint deviceId: "0x2001", inClusters: "0x30 0x60 0x85 0x8E 0x72 0x70 0x86 0x7A 0xEF"
}

simulator {
}

tiles {
	standardTile("contact", "device.contact", width: 1, height: 1) {
		state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
		state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
}
	standardTile("contact2", "device.contact", width: 1, height: 1) {
		state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
		state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
}

standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
	state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
}

main(["contact", "contact2"])
details(["contact","contact2","configure"])
}
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [ 0x60: 3])
	if (cmd) {
		result = zwaveEvent(cmd)
	}
	log.debug "parsed '$description' to result: ${result}"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv1.ManufacturerSpecificReport cmd) {
	log.debug("ManufacturerSpecificReport ${cmd.inspect()}")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug("ConfigurationReport ${cmd.inspect()}")
}

def configure() {
	log.debug "configure"
    def cmds = []
	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
	cmds << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
	cmds << zwave.associationV1.associationRemove(groupingIdentifier:1, nodeId:zwaveHubNodeId).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()

    delayBetween(cmds, 500)
	}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	log.debug "BasicSet V1 ${cmd.inspect()}"
	if (cmd.value) {
	createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
	} else {
	createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	log.debug "ZWaveEvent V3 ${cmd.inspect()}"
	def result
	if (cmd.commandClass == 32) {
		if (cmd.parameter == [0]) {
			if (cmd.sourceEndPoint == 1) {
				result = createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
				log.debug "Contact is closed"
			}
			else
			if (cmd.sourceEndPoint == 2) {
				result = createEvent(name: "contact2", value: "closed", descriptionText: "$device.displayName is closed")
				log.debug "Contact is closed"
			}
		}
		if (cmd.parameter == [255]) {
			if (cmd.sourceEndPoint == 1) {
				result = createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
				log.debug "Contact is open"
			}
			else
			if (cmd.sourceEndPoint == 2) {
				result = createEvent(name: "contact2", value: "open", descriptionText: "$device.displayName is open")
				log.debug "Contact is open"
			}
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// This will capture any commands not handled by other instances of zwaveEvent
	// and is recommended for development so you can see every command the device sends
	return createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def listCurrentParams() {
	log.debug "Listing of current parameter settings of ${device.displayName}"
    def cmds = []
	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier:2).format()
	cmds << zwave.associationV2.associationGet(groupingIdentifier: 3).format()
	cmds << zwave.associationV1.associationGet(groupingIdentifier: 1).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 4).format()
	
	delayBetween(cmds, 500)
}