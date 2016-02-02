/**
 *  MCOHome MH-S412
 *
 *  Author Stuart Buchanan based on work of Matt Frank, chrisb for AEON Power Strip
 *
 *  Date Created:  12/22/2015
 *  Last Modified: 12/22/2015
 *
 */
 // for the UI
metadata {
  definition (name: "MCOHome MH-S412 Dual Relay", namespace: "fuzzysb", author: "Stuart Buchanan") {
    capability "Switch"
    capability "Polling"
    capability "Configuration"
    capability "Refresh"
    capability "Zw Multichannel"

    attribute "switch", "string"
    attribute "switch2", "string"

    command "on"
    command "off"
    command "on2"
    command "off2"

    fingerprint deviceId: "0x1001", inClusters:"0x25 0x27 0x85 0x60 0x8E 0x72 0x86 0xEF 0x20 0x60"
  }

  simulator {
  }

  tiles {
  		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
        standardTile("switch2", "device.switch2",canChangeIcon: true) {
                        state "on", label: "switch2", action: "off2", icon: "st.switches.switch.on", backgroundColor: "#79b821"
                        state "off", label: "switch2", action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
                }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
                        state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
                }

        standardTile("configure", "device.switch", inactiveLabel: false, decoration: "flat") {
                state "default", label:"", action:"configure", icon:"st.secondary.configure"
                }

        main("switch")
        details(["switch","switch2","refresh","configure"])
  }
}

def parse(String description) {
    def result = null
    def cmd = zwave.parse(description, [0x20:1, 0x60:3, 0x25:1, 0x70:1, 0x32:1, 0x72:1])
    if (cmd) {
        result = zwaveEvent(cmd)
        log.debug "Parsed ${description} to ${cmd} to ${result.inspect()}"
    } else {
        log.debug "Non-parsed event: ${description}"
    }
    return result
}


//Reports

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    return createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	log.debug "MultiChannelCmdEncap $cmd"
	def name = "switch$cmd.sourceEndPoint"
    if (cmd.sourceEndPoint == 1) name = "switch"
    def map = [ name: name ]
    if (cmd.commandClass == 37) {
    	if (cmd.parameter == [0]) {
        	map.value = "off"
        }
        if (cmd.parameter == [255]) {
            map.value = "on"
        }
        return createEvent(map)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd) {
	log.debug "MultiChannelCapabilityReport $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
        def result = []
        result << createEvent(name:"switch", value: cmd.value ? "on" : "off")

        // For a multilevel switch, cmd.value can be from 1-99 to represent dimming levels
        result << createEvent(name:"level", value: cmd.value, unit:"%", descriptionText:"${device.displayName} dimmed ${cmd.value==255 ? 100 : cmd.value}%")

        result
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
        createEvent(name:"switch", value: cmd.value ? "on" : "off")
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd)
{
        def result = []
        result << createEvent(name:"switch", value: cmd.value ? "on" : "off")
        result << createEvent(name:"level", value: cmd.value, unit:"%", descriptionText:"${device.displayName} dimmed ${cmd.value==255 ? 100 : cmd.value}%")
        result
}


def refresh() {
  log.debug "refresh"
  delayBetween([
    getendpointreport(1),
    getendpointreport(2)
  ])
}


def poll() {
  log.debug "poll"
	refresh()
}

def configure() {
  log.debug "configure"
    delayBetween([
	    zwave.associationV1.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId).format(),
		zwave.associationV1.associationSet(groupingIdentifier: 2, nodeId: zwaveHubNodeId).format(),
		zwave.associationV1.associationSet(groupingIdentifier: 3, nodeId: zwaveHubNodeId).format()
    ])
}

def on() {
  delayBetween([
	endpointon(1),
	getendpointreport(1)
  ])
}

def off() {
  delayBetween([
	endpointoff(1),
	getendpointreport(1)
    ])
}

def on2() {
  delayBetween([
	endpointon(2),
	getendpointreport(2)
    ])
}

def off2() {
  delayBetween([
	endpointoff(2),
	getendpointreport(2)
  ])
}


def endpointon(endpoint) {
    log.debug "MCO2-on $endpoint"
    encap(zwave.basicV1.basicSet(value: 0xFF), endpoint).format()
}

def endpointoff(endpoint) {
    log.debug "MCO2-off $endpoint"
    encap(zwave.basicV1.basicSet(value: 0x00), endpoint).format()
}

def getendpointreport(endpoint) {
    log.debug "MCO2-off $endpoint"
    encap(zwave.basicV1.basicGet(), endpoint).format()
}

private encap(cmd, endpoint) {
    log.debug "MCO2-encap $endpoint {$cmd}"
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:endpoint, destinationEndPoint:endpoint).encapsulate(cmd)
}
