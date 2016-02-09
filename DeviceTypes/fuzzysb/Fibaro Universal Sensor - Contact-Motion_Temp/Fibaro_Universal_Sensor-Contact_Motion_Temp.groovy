metadata {
	definition (name: "Fibaro Universal Sensor - Contact-Motion-Temp", namespace: "fuzzysb", author: "Stuart Buchanan") {
    capability 	"Contact Sensor"
	capability 	"Motion Sensor"
	capability 	"Temperature Measurement"
	capability 	"Configuration"
	capability 	"Sensor"
    
    command "updateCurrentParams"
    command "listCurrentParams"

        /* Capability notes
        0x30 Sensor Binary V1 V2
        0x60 Multi Channel V3
        0x85 Association V1 V2
        0x8E Multi Instance Association V1
        0x72 Manufacturer Specific V1 V2
        0x70 Configuration V1 V2
        0x86 Version V1
        0x7A Firmware Update Md V1 V2
        0xEF Mark V1
        0x2B Scene Activation V1
        */
	
	fingerprint deviceId: "0x2001", inClusters: "0x20 0x30 0x31 0x60 0x85 0x8E 0x72 0x70 0x86 0x7A 0xEF 0x2B"
	}

	simulator {
	}

	tiles(scale: 2) {
      	multiAttributeTile(name: "thermostat", type:"thermostat", width:6, height:4) {
			tileAttribute("device.temperature", key:"PRIMARY_CONTROL", canChangeIcon: true, canChangeBackground: true){
            	attributeState "default", label:'${currentValue}°', unit:"C", backgroundColor:"#fab907", icon:"st.Weather.weather2"
            }
			tileAttribute ("device.contact", key: "SECONDARY_CONTROL") {
            	attributeState "open", label: '${name}'
				attributeState "closed", label: '${name}'
			}
    	}
       standardTile("contact", "device.contact", width: 2, height: 2) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
		}
        standardTile("motion", "device.motion", width: 2, height: 2) {
    		state "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
			state "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2 ) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

	main(["thermostat"])
	details(["thermostat", "contact" ,"motion", "configure"])
	}
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [ 0x20: 1, 0x30: 1, 0x31: 2, 0x60: 3, 0x85: 2, 0x8E: 2, 0x72: 1, 0x70: 1, 0x86: 1, 0x7A: 1, 0xEF: 1, 0x2B: 1 ])
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
	cmds << zwave.configurationV1.configurationSet(configurationValue: [8], parameterNumber: 12, size: 1).format()
	cmds << zwave.associationV1.associationRemove(groupingIdentifier:1, nodeId:zwaveHubNodeId).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 15).format()
    delayBetween(cmds, 500)
}

def createEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd, Map item1) { 
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
    log.debug "manufacturerName: ${cmd.manufacturerName}"
    log.debug "productId:        ${cmd.productId}"
    log.debug "productTypeId:    ${cmd.productTypeId}"
}

def createEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd, Map item1) {	
    updateDataValue("applicationVersion", "${cmd.applicationVersion}")
    log.debug "applicationVersion:      ${cmd.applicationVersion}"
    log.debug "applicationSubVersion:   ${cmd.applicationSubVersion}"
    log.debug "zWaveLibraryType:        ${cmd.zWaveLibraryType}"
    log.debug "zWaveProtocolVersion:    ${cmd.zWaveProtocolVersion}"
    log.debug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
/*	log.debug "BasicSet V1 ${cmd.inspect()}"
	if (cmd.value) {
	createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
	} else {
	createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
	}
    */
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
    log.debug "Sensor Binary report"
	def map = [:]
	map.value = cmd.sensorValue ? "active" : "inactive"
	map.name = "motion"
	if (map.value == "active") {
		map.descriptionText = "$device.displayName detected motion"
	}
	else {
		map.descriptionText = "$device.displayName motion has stopped"
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd)
{
	log.debug "Sensor MultiLevel Report"
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			// temperature
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision).toDouble().round(1)
            log.debug("tempvalue: " + map.value)
			map.unit = getTemperatureScale()
            log.debug("tempunit: " + map.unit)
			map.name = "temperature"
			break;
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv1.SensorMultilevelReport cmd) {
    log.debug "SensorMultilevelReport $cmd"
}


def createEvent(physicalgraph.zwave.commands.firmwareupdatemdv1.FirmwareMdReport cmd, Map item1) { 
    log.debug "checksum:       ${cmd.checksum}"
    log.debug "firmwareId:     ${cmd.firmwareId}"
    log.debug "manufacturerId: ${cmd.manufacturerId}"
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
        return result
	}else{
		def encapsulatedCommand = cmd.encapsulatedCommand([ 0x20: 1, 0x30: 1, 0x31: 2, 0x60: 3, 0x85: 2, 0x8E: 2, 0x72: 1, 0x70: 1, 0x86: 1, 0x7A: 1, 0xEF: 1, 0x2B: 1]) // can specify command class versions here like in zwave.parse
		log.debug ("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")
		if (encapsulatedCommand) {
			return zwaveEvent(encapsulatedCommand)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Catchall reached for cmd: ${cmd.toString()}}"
	[:]
}


def listCurrentParams() {
	log.debug "Listing of current parameter settings of ${device.displayName}"
    def cmds = []
	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier:2).format()
	cmds << zwave.associationV2.associationGet(groupingIdentifier: 3).format()
	cmds << zwave.associationV1.associationGet(groupingIdentifier: 1).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 1).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 2).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 4).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 5).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 6).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 7).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 8).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 9).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 10).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 11).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 12).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 13).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 14).format()
   
	delayBetween(cmds, 500)
}

def updateCurrentParams() {
	log.debug "Updating current parameter settings of ${device.displayName}"
    def cmds = []
    cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
    cmds << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
    cmds << zwave.associationV2.associationRemove(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()   
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0, 0], parameterNumber: 1, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0, 0], parameterNumber: 2, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [255], parameterNumber: 5, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [255], parameterNumber: 6, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [255], parameterNumber: 7, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [255], parameterNumber: 8, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 9, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [20], parameterNumber: 10, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [60], parameterNumber: 11, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [8], parameterNumber: 12, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 13, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 14, size: 1).format()
   
	delayBetween(cmds, 500)
}