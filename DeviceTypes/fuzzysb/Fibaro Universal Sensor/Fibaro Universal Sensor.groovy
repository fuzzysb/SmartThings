metadata {
	definition (name: "Fibaro Universal Sensor", namespace: "fuzzysb", author: "Stuart Buchanan") {
    capability 	"Contact Sensor"
	capability 	"Motion Sensor"
    capability 	"Configuration"
	capability 	"Temperature Measurement"
    
	command "report"
	command "singleSet"
	command "singleRemove"
	command "multiSet"
	command "multiRemove"
	command "updateZwaveParam"
    command "updateCurrentParams"
    command "listCurrentParams"
	
	fingerprint deviceId: "0x2001", inClusters: "0x30 0x60 0x85 0x8E 0x72 0x70 0x86 0x7A 0xEF"
}

simulator {
	// These show up in the IDE simulator "messages" drop-down to test
	// sending event messages to your device handler
	status "open" : "zw device: 02, command: 2001, payload: 00"
	status "closed": "zw device: 02, command: 2001, payload: FF"
	status "basic report on":
	zwave.basicV1.basicReport(value:0xFF).incomingMessage()
	status "basic report off":
	zwave.basicV1.basicReport(value:0).incomingMessage()
	status "dimmer switch on at 70%":
	zwave.switchMultilevelV1.switchMultilevelReport(value:70).incomingMessage()
	status "basic set on":
	zwave.basicV1.basicSet(value:0xFF).incomingMessage()
	status "temperature report 70°F":
	zwave.sensorMultilevelV2.sensorMultilevelReport(scaledSensorValue: 70.0, precision: 1, sensorType: 1, scale: 1).incomingMessage()
	status "low battery alert":
	zwave.batteryV1.batteryReport(batteryLevel:0xFF).incomingMessage()
	status "multichannel sensor":
	zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1).encapsulate(zwave.sensorBinaryV1.sensorBinaryReport(sensorValue:0)).incomingMessage()
	// simulate turn on
	reply "2001FF,delay 5000,2002": "command: 2503, payload: FF"

        // simulate turn off
        reply "200100,delay 5000,2002": "command: 2503, payload: 00"
}

tiles {
	standardTile("contact", "device.contact", width: 1, height: 1) {
	state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
	state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
}
standardTile("motion", "device.motion", width: 1, height: 1) {
    state "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
	state "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
}
valueTile("temperature", "device.temperature", inactiveLabel: false) {
	state "temperature", label:'${currentValue}°',
	backgroundColors:[
		[value: "", color: "#ffffff"],
        [value: 31, color: "#153591"],
		[value: 44, color: "#1e9cbb"],
		[value: 59, color: "#90d2a7"],
		[value: 74, color: "#44b621"],
		[value: 84, color: "#f1d801"],
		[value: 95, color: "#d04e00"],
		[value: 96, color: "#bc2323"]
	]
}

standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
	state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
}

main(["contact", "motion", "temperature"])
details(["contact","motion"])
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

def report() {
	// zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	//
	delayBetween([
	zwave.configurationV1.configurationGet(parameterNumber: 5).format(),
	zwave.configurationV1.configurationGet(parameterNumber: 6).format()
	])
	}

def configure() {
	log.debug "configure"
    def cmds = []
	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
	//zwave.associationV2.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
	cmds << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
	// temperature change sensitivity
	cmds << zwave.configurationV1.configurationSet(configurationValue: [4], parameterNumber: 12, size: 1).format()
	//cmds << zwave.configurationV1.configurationGet(parameterNumber: 12).format()
    // remove group 1 association to stop redundant BasicSet
	cmds << zwave.associationV1.associationRemove(groupingIdentifier:1, nodeId:zwaveHubNodeId).format()
		// see if there is a temp probe on board and is it working
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 15).format()
    delayBetween(cmds, 500)
	}

def singleSet() {
	def cmds = []

	//cmds << zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
	//cmds << zwave.associationV2.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
	cmds << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()

	delayBetween(cmds, 500)
}

def singleRemove() {
	def cmds = []

	//cmds << zwave.associationV2.associationRemove(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
	//cmds << zwave.associationV2.associationRemove(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
	cmds << zwave.associationV2.associationRemove(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()

	delayBetween(cmds, 500)
}

def multiSet() {
	def cmds = []

	//cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
	//cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()

	delayBetween(cmds, 500)
}

def multiRemove() {
	def cmds = []

	//cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
	//cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()

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

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd)
{
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			// temperature
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			map.name = "temperature"
			break;
	}
	createEvent(map)
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
				result = createEvent(name: "motion", value: "active", descriptionText: "$device.displayName is active")
				log.debug "motion is active"
			}
		}
		if (cmd.parameter == [255]) {
			if (cmd.sourceEndPoint == 1) {
				result = createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
				log.debug "Contact is open"
			}
			else
			if (cmd.sourceEndPoint == 2) {
				result = createEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName is inactive")
				log.debug "motion is inactive"
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

def updateZwaveParam(params) {
	if ( params ) {
        def pNumber = params.paramNumber
        def pSize	= params.size
        def pValue	= [params.value]
        log.debug "Make sure device is awake and in recieve mode"
        log.debug "Updating ${device.displayName} parameter number '${pNumber}' with value '${pValue}' with size of '${pSize}'"

		def cmds = []
        cmds << zwave.configurationV1.configurationSet(configurationValue: pValue, parameterNumber: pNumber, size: pSize).format()
        cmds << zwave.configurationV1.configurationGet(parameterNumber: pNumber).format()
        delayBetween(cmds, 1000)        
    }
}


def listCurrentParams() {
	log.debug "Listing of current parameter settings of ${device.displayName}"
    def cmds = []
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 4).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 13).format()
   
	delayBetween(cmds, 500)
}

def updateCurrentParams() {
	log.debug "Updating current parameter settings of ${device.displayName}"
    def cmds = []
    cmds << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [3], parameterNumber: 4, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 13, size: 1).format()
   
	delayBetween(cmds, 500)
}