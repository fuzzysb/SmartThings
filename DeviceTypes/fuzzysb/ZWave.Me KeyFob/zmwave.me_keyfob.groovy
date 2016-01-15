/**
 *  Copyright 2015 Stuart Buchanan
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
 *	Z-Wave.me Secure Key Fob
 *
 *	Author: fuzzysb
 *	Date: 2016-01-14
 *
 *  V1.2 added fix for newer firmware because button 1 was not working.
 *
 *  V1.1 Added functions to recieve and send security encapsulated messages
 *
 *  V1.0 Initial Release, each button can be pressed, held or double clicked giving 12 actions that can be assigned in smartthings
 *  Can also be used for a Devolo Home Control 9360 Remote
 */

metadata {
	definition (name: "Z-Wave.me Key Fob", namespace: "fuzzysb", author: "Stuart Buchanan") {
		capability "Actuator"
		capability "Button"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"

		command		"resetParams2StDefaults"
		command		"listCurrentParams"
        
        /* Capability notes
        0x5E COMMAND_CLASS_ZWAVE_PLUS_INFO 2
        0x8F COMMAND_CLASS_MULTI_CMD 1
        0x73 COMMAND_CLASS_POWERLEVEL 1
        0x98 COMMAND_CLASS_SECURITY 1
        0x86 COMMAND_CLASS_VERSION 2
        0x72 COMMAND_CLASS_MANUFACTURER_SPECIFIC 2
        0x70 COMMAND_CLASS_CONFIGURATION 1
		0x85 COMMAND_CLASS_ASSOCIATION 2
        0x2D COMMAND_CLASS_SCENE_CONTROLLER_CONF 1
        0x8E COMMAND_CLASS_MULTI_INSTANCE_ASSOCIATION 2
        0x80 COMMAND_CLASS_BATTERY 1
        0x84 COMMAND_CLASS_WAKE_UP 2
        0x5A COMMAND_CLASS_DEVICE_RESET_LOCALLY 1
        0x59 COMMAND_CLASS_ASSOCIATION_GRP_INFO  1
        0x5B COMMAND_CLASS_CENTRAL_SCENE 1
        0xEF COMMAND_CLASS_MARK 1
        0x20 COMMAND_CLASS_BASIC 1
        0x5B COMMAND_CLASS_CENTRAL_SCENE 1
		0x26 COMMAND_CLASS_SWITCH_MULTILEVEL 2
        0x27 COMMAND_CLASS_SWITCH_ALL 1
        0x2B COMMAND_CLASS_SCENE_ACTIVATION 1
        0x60 COMMAND_CLASS_MULTI_CHANNEL 3
        */
                                                     
		fingerprint deviceId: "0x1202", inClusters: "0x5E 0x8F 0x73 0x98 0x86 0x72 0x70 0x85 0x2D 0x8E 0x80 0x84 0x5A 0x59 0x5B 0xEF 0x20 0x5B 0x26 0x27 0x2B 0x60"
}

simulator {
		status "button 1 Pushed":  "command: 2B01, payload: 0B FF"
		status "button 1 Held":  "command: 2B01, payload: 0D FF"
        status "button 1 Double Clicked":  "command: 2B01, payload: 0C FF"
		status "button 2 Pushed":  "command: 2B01, payload: 15 FF"
		status "button 2 Held":  "command: 2B01, payload: 17 FF"
        status "button 2 Double Clicked":  "command: 2B01, payload: 16 FF"
        status "button 3 Pushed":  "command: 2B01, payload: 1F FF"
		status "button 3 Held":  "command: 2B01, payload: 21 FF"
        status "button 3 Double Clicked":  "command: 2B01, payload: 20 FF"
        status "button 4 Pushed":  "command: 2B01, payload: 29 FF"
		status "button 4 Held":  "command: 2B01, payload: 2B FF"
        status "button 4 Double Clicked":  "command: 2B01, payload: 2A FF"
		status "wakeup":  "command: 8407, payload: "
}

	tiles {
		standardTile("button", "device.button", width: 2, height: 2) {
			state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
			state "button 1 pushed", label: "pushed #1", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#79b821"
			state "button 2 pushed", label: "pushed #2", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#79b821"
			state "button 3 pushed", label: "pushed #3", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#79b821"
			state "button 4 pushed", label: "pushed #4", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#79b821"
			state "button 1 held", label: "held #1", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffa81e"
			state "button 2 held", label: "held #2", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffa81e"
			state "button 3 held", label: "held #3", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffa81e"
			state "button 4 held", label: "held #4", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffa81e"
			state "button 1 doubleclick", label: "double clicked #1", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#216ab8"
			state "button 2 doubleclick", label: "double clicked #2", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#216ab8"
			state "button 3 doubleclick", label: "double clicked #3", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#216ab8"
			state "button 4 doubleclick", label: "double clicked #4", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#216ab8"	
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        standardTile("configure", "device.button", width: 1, height: 1, decoration: "flat") {
        	state "default", label: "configure", backgroundColor: "#ffffff", action: "configure"
		}
		main "button"
		details(["button", "battery", "configure"])
	}
}

/*
//Old Parse Method
def parse(String description) {
	def results = []
	log.debug("RAW command: $description")
	if (description.startsWith("Err")) {
		log.debug("An error has occurred")
	} 
	else {
   		def cmd = zwave.parse(description, [0x5E: 2, 0x8F: 1, 0x73: 1, 0x98: 1, 0x86: 2, 0x72: 2, 0x70: 1, 0x85: 2, 0x2D: 1, 0x8E: 2, 0x80: 1, 0x84: 2, 0x5A: 1, 0x59: 1, 0x5B: 1, 0xEF: 1, 0x20: 1, 0x5B: 1, 0x26: 2, 0x27: 1, 0x2B: 1, 0x60: 3])
		log.debug "Parsed Command: $cmd"
		if (cmd) {
			results = zwaveEvent(cmd)
		}
	}
}
*/

def parse(String description)
{
	def result = null
	if (description.startsWith("Err 106")) {
		state.sec = 0
		result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
			descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x5E: 2, 0x8F: 1, 0x73: 1, 0x98: 1, 0x86: 2, 0x72: 2, 0x70: 1, 0x85: 2, 0x2D: 1, 0x8E: 2, 0x80: 1, 0x84: 2, 0x5A: 1, 0x59: 1, 0x5B: 1, 0xEF: 1, 0x20: 1, 0x5B: 1, 0x26: 2, 0x27: 1, 0x2B: 1, 0x60: 3])
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
	def encapsulatedCommand = cmd.encapsulatedCommand([0x5E: 2, 0x8F: 1, 0x73: 1, 0x98: 1, 0x86: 2, 0x72: 2, 0x70: 1, 0x85: 2, 0x2D: 1, 0x8E: 2, 0x80: 1, 0x84: 2, 0x5A: 1, 0x59: 1, 0x5B: 1, 0xEF: 1, 0x20: 1, 0x5B: 1, 0x26: 2, 0x27: 1, 0x2B: 1, 0x60: 3])
	state.sec = 1
	log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'"
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	[ createEvent(descriptionText: "${device.displayName} woke up"),
	response(zwave.wakeUpV1.wakeUpNoMoreInformation()) ]
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) { // Special value for low battery alert
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
		log.debug ("Battery: $cmd.batteryLevel")
	}
	// Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
	state.lastbatt = new Date().time
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
	log.debug( "Dimming Duration: $cmd.dimmingDuration")
	log.debug( "Button code: $cmd.sceneId")

	if ( cmd.sceneId == 11 ) {
		Integer button = 1
		sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
		log.debug( "Button $button was pushed" )
	}
	else if  ( cmd.sceneId == 13 ) {
		Integer button = 1
		sendEvent(name: "button", value: "held", data: [buttonNumber: button], descriptionText: "$device.displayName Button $button is closed", isStateChange: true)
		log.debug( "Button $button Hold start" )
	}
	else if  ( cmd.sceneId == 15 ) {
		Integer button = 1
		sendEvent(name: "button", value: "holdRelease", data: [buttonNumber: button], descriptionText: "$device.displayName Button $button is open")
		log.debug( "Button $button Hold stop" )
	}
    else if  ( cmd.sceneId == 12 ) {
		Integer button = 1
		sendEvent(name: "button", value: "doubleclick", data: [buttonNumber: button], descriptionText: "$device.displayName Button $button was Double Clicked", isStateChange: true)
		log.debug( "Button $button was Double Clicked" )
	}
	else if ( cmd.sceneId == 21 ) {
		Integer button = 2
		sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
		log.debug( "Button $button was pushed" )
	}
	else if  ( cmd.sceneId == 23 ) {
		Integer button = 2
		sendEvent(name: "button", value: "held", data: [buttonNumber: button], descriptionText: "$device.displayName Button $button is closed", isStateChange: true)
		log.debug( "Button $button Hold start" )
	}
	else if  ( cmd.sceneId == 25 ) {
		Integer button = 2
		sendEvent(name: "button", value: "holdRelease", data: [buttonNumber: button], descriptionText: "$device.displayName Button $button is open")
		log.debug( "Button $button Hold stop" )
	}
    else if  ( cmd.sceneId == 22 ) {
		Integer button = 2
		sendEvent(name: "button", value: "doubleclick", data: [buttonNumber: button], descriptionText: "$device.displayName Button $button was Double Clicked", isStateChange: true)
		log.debug( "Button $button was Double Clicked" )
	}
    	else if ( cmd.sceneId == 31 ) {
		Integer button = 3
		sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
		log.debug( "Button $button was pushed" )
	}
	else if  ( cmd.sceneId == 33 ) {
		Integer button = 3
		sendEvent(name: "button", value: "held", data: [buttonNumber: button], descriptionText: "$device.displayName Button $button is closed", isStateChange: true)
		log.debug( "Button $button Hold start" )
	}
	else if  ( cmd.sceneId == 35 ) {
		Integer button = 3
		sendEvent(name: "button", value: "holdRelease", data: [buttonNumber: button], descriptionText: "$device.displayName Button $button is open")
		log.debug( "Button $button Hold stop" )
	}
    else if  ( cmd.sceneId == 32 ) {
		Integer button = 3
		sendEvent(name: "button", value: "doubleclick", data: [buttonNumber: button], descriptionText: "$device.displayName Button $button was Double Clicked", isStateChange: true)
		log.debug( "Button $button was Double Clicked" )
	}
        	else if ( cmd.sceneId == 41 ) {
		Integer button = 4
		sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
		log.debug( "Button $button was pushed" )
	}
	else if  ( cmd.sceneId == 43 ) {
		Integer button = 4
		sendEvent(name: "button", value: "held", data: [buttonNumber: button], descriptionText: "$device.displayName Button $button is closed", isStateChange: true)
		log.debug( "Button $button Hold start" )
	}
	else if  ( cmd.sceneId == 45 ) {
		Integer button = 4
		sendEvent(name: "button", value: "holdRelease", data: [buttonNumber: button], descriptionText: "$device.displayName Button $button is open")
		log.debug( "Button $button Hold stop" )
	}
    else if  ( cmd.sceneId == 42 ) {
		Integer button = 4
		sendEvent(name: "button", value: "doubleclick", data: [buttonNumber: button], descriptionText: "$device.displayName Button $button was Double Clicked", isStateChange: true)
		log.debug( "Button $button was Double Clicked" )
	}
	else {
		log.debug( "Commands and Button ID combinations unaccounted for happened" )
	}
}

//Uncomment following Section to debug
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "return result of Zwave Event"
	return createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def configure() {
	log.debug "Resetting Sensor Parameters to SmartThings Compatible Defaults"
	def cmds = []
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 2, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 3, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 4, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 5, nodeId: zwaveHubNodeId).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 1, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 2, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [4], parameterNumber: 11, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [4], parameterNumber: 12, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [4], parameterNumber: 13, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [4], parameterNumber: 14, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 21, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 22, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 24, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 25, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 30, size: 1).format()
    
    delayBetween(cmds, 500)
}
       
    
    def resetParams2StDefaults() {
	log.debug "Resetting Sensor Parameters to SmartThings Compatible Defaults"
	def cmds = []
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 2, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 3, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 4, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 5, nodeId: zwaveHubNodeId).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 1, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 2, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [4], parameterNumber: 11, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [4], parameterNumber: 12, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [4], parameterNumber: 13, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [4], parameterNumber: 14, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 21, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 22, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 24, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 25, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 30, size: 1).format()
    
    delayBetween(cmds, 500)
	}

def listCurrentParams() {
	log.debug "Listing of current parameter settings of ${device.displayName}"
    def cmds = []
	cmds << zwave.associationV1.associationGet(groupingIdentifier: 1).format()
    cmds << zwave.associationV1.associationGet(groupingIdentifier: 2).format()
    cmds << zwave.associationV1.associationGet(groupingIdentifier: 3).format()
    cmds << zwave.associationV1.associationGet(groupingIdentifier: 4).format()
    cmds << zwave.associationV1.associationGet(groupingIdentifier: 5).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 1).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 2).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 11).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 12).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 13).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 14).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 21).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 22).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 24).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 25).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 30).format()
    
	delayBetween(cmds, 500)
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