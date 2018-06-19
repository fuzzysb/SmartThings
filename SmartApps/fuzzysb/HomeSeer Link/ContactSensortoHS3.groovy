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
 *	Send ST Contact Sensor to HomeSeer HS3 SecuritySensor
 *
 *	Author: Stuart Buchanan
 /*
 *	Date: 2016-09-015 v1.0 Initial Release
 */
definition(
    name: "ST to HomeSeer HS3 Contact Sensor",
    namespace: "fuzzysb",
    author: "Stuart Buchanan",
    description: "Triggers Virtual Contact Sensor on HomeSeer HS3 when ST Contact Sensor status changes",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section(title: "Select Devices") {
        input "ContactSensor", "capability.contactSensor", title: "Select ST Contact Sensor", required: true, multiple:false
    }
    section("Select HomeSeer Device"){
			input "HomeSeer", "capability.actuator", title: "HomeSeer", required: true, multiple: false, defaultValue: false, submitOnChange: true
    }   
    section(title: "Enter Corresponding HomeSeer Device ID") {
    input name: "HS3Id", type: "text", title: "HomeSeer Device ID", required: true
    }
}

def installed() {
  log.debug "Installed with settings: ${settings}"
  init()
}

def updated() {
  log.debug "Updated with settings: ${settings}"
  unsubscribe()
  init()
}

def init() {
  subscribe(ContactSensor, "contact", handleEvent)
}


def lanResponseHandler(evt) {
	log.debug "In response handler"
	log.debug "I got back ${evt.description}"
}

def handleEvent(evt) {
  	log.debug "Event Value is: ${evt.value}"
    def result
	if (evt.value == "open")
    {
    	sendOpenCommand()
    }
    else
    {
    	sendClosedCommand()
    }
    
}


def sendOpenCommand() {
					log.debug "Sending open Web Request to HS3 Id ${settings.HS3Id}"
                    HomeSeer.api("open", settings.HS3Id)
}

def sendClosedCommand() {
					log.debug "Sending closed Web Request to HS3 Id ${settings.HS3Id}"
                    HomeSeer.api("closed", settings.HS3Id)
}