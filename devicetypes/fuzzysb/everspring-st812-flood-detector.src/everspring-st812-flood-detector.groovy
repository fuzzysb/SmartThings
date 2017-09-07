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
 *	Everspring ST812 Flood Detector
 *
 *	Author: Stuart Buchanan, Based on original work by Tosa with thanks
 *	Date: 2016-01-31
 */

preferences {
    // manufacturer default wake up is every hour; optionally increase for better battery life
    input "userWakeUpInterval", "number", title: "Wake Up Interval (seconds)", description: "Default 3600 sec (60 sec - 194 days)", defaultValue: '3600', required: false, displayDuringSetup: true
}


metadata {
	definition (name: "Everspring ST812 Flood Detector", namespace: "fuzzysb", author: "Stuart Buchanan") {
		capability "Water Sensor"
		capability "Battery"
		capability "Configuration"

        fingerprint deviceId: "0xA102", inClusters: "0x86,0x72,0x85,0x84,0x80,0x70,0x9C,0x20,0x71"
	}

	simulator {
		status "dry": "command: 9C02, payload: 00 05 00 00 00"
		status "wet": "command: 9C02, payload: 00 05 FF 00 00"
        status "wakeup": "command: 8407, payload: "
        status "low batt alarm": "command: 7105, payload: 01 FF"
		status "battery <20%": new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: 0xFF).incomingMessage()
        for (int i = 20; i <= 100; i += 10) {
			status "battery ${i}%": new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: i).incomingMessage()
		}
    }

	tiles {
		standardTile("water", "device.water", width: 2, height: 2) {
			state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
			state "wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, canChangeBackground: true) {
			state "battery", label:'${currentValue}% Battery', unit:"",
            backgroundColors:[
				[value: 19, color: "#BC2323"],
				[value: 20, color: "#D04E00"],
				[value: 30, color: "#D04E00"],
				[value: 40, color: "#DAC400"],
				[value: 41, color: "#79b821"]
			]
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		main "water"
		details(["water", "battery", "configure"])
	}
}

def parse(String description) {
	log.debug "parse: $description"

	def parsedZwEvent = zwave.parse(description, [0x9C: 1, 0x71: 1, 0x84: 2, 0x30: 1, 0x70: 1, 0x71: 1])
	def result = []

    if (parsedZwEvent) {
        result = zwaveEvent(parsedZwEvent)
        log.debug "Parsed ${parsedZwEvent} to ${result.inspect()}"
    } else {
        log.debug "Non-parsed event: ${description}"
    }

	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {

    // Appears that the Everspring/Utilitech water sensor responds to batteryGet, but not wakeUpNoMoreInformation(?)

    def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange:  false)]
    result << response(zwave.batteryV1.batteryGet())

    // If user has changed userWakeUpInterval, send the new interval to the device 
	def userWake = getUserWakeUp(userWakeUpInterval)
    if (state.wakeUpInterval != userWake) {
        state.wakeUpInterval = userWake
        result << response("delay 200")
        result << response(zwave.wakeUpV2.wakeUpIntervalSet(seconds:state.wakeUpInterval, nodeid:zwaveHubNodeId))
        result << response("delay 200")
        result << response(zwave.wakeUpV2.wakeUpIntervalGet())
    }

    return result
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd) {

	def map = [:]
	if (cmd.sensorType == 0x05) {
		map.name = "water"
		map.value = cmd.sensorState ? "wet" : "dry"
		map.descriptionText = "${device.displayName} is ${map.value}"
	}

	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {

	def map = [:]
	map.name = "water"
	map.value = cmd.sensorValue ? "wet" : "dry"
	map.descriptionText = "${device.displayName} is ${map.value}"

	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd) {

	def map = [:]
    def result
	if (cmd.alarmType == 1) {
        if (cmd.alarmLevel == 0xFF) {
		    map.descriptionText = "${device.displayName} has a low battery alarm"
		    map.displayed = true
        } else if (cmd.alarmLevel == 1) {
		    map.descriptionText = "${device.displayName} battery alarm level 1"   // device sometimes returns alarmLevel 1, 
		    map.displayed = false                                                 //   but this appears to be an undocumented alarmLevel(?)
        }
        result = [createEvent(map)]
        result << response(zwave.batteryV1.batteryGet())                          // try to update battery status, but device doesn't seem to always respond
    } else if (cmd.alarmType == 2 && cmd.alarmLevel == 1) {
        map.descriptionText = "${device.displayName} powered up"
		map.displayed = false
        result = [createEvent(map)]
	} else {
		log.debug cmd
	}

    return result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {

	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {          // Special value for low battery alert
		map.value = 10                       // will display (and alarm in mobile app) as 10% battery remaining, even though it's really 1%-19% remaining
		map.descriptionText = "${device.displayName} reports a low battery"
        map.isStateChange = true
		map.displayed = true
	} else {
		map.value = cmd.batteryLevel
		map.displayed = false
	}

    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalCapabilitiesReport cmd) {

    def map = [ name: "defaultWakeUpInterval", unit: "seconds" ]
	map.value = cmd.defaultWakeUpIntervalSeconds
	map.displayed = false

	state.defaultWakeUpInterval = cmd.defaultWakeUpIntervalSeconds
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {

	def map = [ name: "reportedWakeUpInterval", unit: "seconds" ]
	map.value = cmd.seconds
	map.displayed = false

    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {

    log.debug "COMMAND CLASS: ${cmd}"
    createEvent(descriptionText: "Command not handled: ${cmd}")
}

def configure() {

    state.wakeUpInterval = getUserWakeUp(userWakeUpInterval)

    delayBetween([
        zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId).format(),
        zwave.wakeUpV2.wakeUpIntervalSet(seconds:state.wakeUpInterval, nodeid:zwaveHubNodeId).format(),
        zwave.wakeUpV2.wakeUpIntervalGet().format(),
        zwave.batteryV1.batteryGet().format()
    ], 200)

}

private getUserWakeUp(userWake) {

    if (!userWake)                       { userWake =     '3600' }  // set default 1 hr if no user preference 

    // make sure user setting is within valid range for device 
    if (userWake.toInteger() <       60) { userWake =       '60' }  // 60 sec min
    if (userWake.toInteger() > 16761600) { userWake = '16761600' }  // 194 days max

	/*
     * Ideally, would like to reassign userWakeUpInterval to min or max when needed,
     * so it more obviously reflects in 'preferences' in the IDE and mobile app
     * for the device. Referencing the preference on the RH side is ok, but
     * haven't figured out how to reassign it on the LH side?
     *
     */
    //userWakeUpInterval = userWake 

    return userWake.toInteger()
}