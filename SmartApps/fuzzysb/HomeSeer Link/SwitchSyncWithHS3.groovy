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
 *
 *	Author: Stuart Buchanan, Based on original work by midyear66 with thanks
 *	Date: 2016-02-03 v1.0 Initial Release
 */
definition(
    name: "Air Conditioning Power",
    namespace: "fuzzysb",
    author: "Stuart Buchanan",
    description: "Triggers Switch when HTTP GET Request is received",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section(title: "Select Devices") {
        input "virtualSwitch", "capability.Switch", title: "Select Switch", required: true, multiple:false
    }
    section("Select HS3 Device"){
		input "HomeSeer", "capability.actuator", title: "HomeSeer", required: true, multiple: false, defaultValue: false, submitOnChange: true
    }
    section(title: "Enter Corresponding HomeSeer Device ID") {
    	input name: "HomeSeerId", type: "text", title: "HomeSeer Device ID", required: true
    }
    
}

def installed() {
	createAccessToken()
	getToken()
	DEBUG("Installed with Application Id: $app.id")
    DEBUG("Installed with token: $state.accessToken")
    DEBUG("Installed with settings: $virtualSwitch.name")
    init()
}
def updated() {
	DEBUG("Updated with Application Id: $app.id")
    DEBUG("Updated with token: $state.accessToken")
    unsubscribe()
  	init()
}

def init() {
  state.lastOnChanged = 0
  state.lastOffChanged = 0
  subscribe(virtualSwitch, "switch", handleEvent)
}

mappings {
  path("/ST/on") {
    action: [
      GET: "switchon"
    ]
  }
  path("/ST/off") {
    action: [
      GET:"switchoff"
    ]
  }
}


// Callback functions
def getSwitch() {
    // This returns the current state of the switch in JSON
    return virtualSwitch.currentState("switch")
}

def switchon() {
            if(now() - (3 * 1000) > state.lastOnChanged){
            	DEBUG("on")
            	state.lastOnChanged = now()
            	virtualSwitch.on();
            }
            else{
            	DEBUG("Not Turning On as State Changed too Recently.")
            }
            
}

def switchoff() {
            if(now() - (3 * 1000) > state.lastOffChanged){
            DEBUG("off")
        	state.lastOffChanged = now()
    		virtualSwitch.off();
        	}
            else{
            	DEBUG("Not Turning Off as State Changed too Recently.")
            }
}

def sendOnCommand() {
					log.debug "Sending home Web Request to HomeSeer Id ${settings.HomeSeerId}"
                    HomeSeer.api("on", settings.HomeSeerId)
}

def sendOffCommand() {
					log.debug "Sending Off Web Request to HomeSeer Id ${settings.HomeSeerId}"
                    HomeSeer.api("off", settings.HomeSeerId)
}

def lanResponseHandler(evt) {
	log.debug "In response handler"
	log.debug "I got back ${evt.description}"
}

def handleEvent(evt) {
    
  	log.debug "Event Value is: ${evt.value}"
    def result
	if (evt.value == "on")
    {
    	sendOnCommand()
    }
    else
    {
    	sendOffCommand()
    }
    
    
}

def getToken(){
if (!state.accessToken) {
		try {
			getAccessToken()
			DEBUG("Creating new Access Token: $state.accessToken")
		} catch (ex) {
			DEBUG("Did you forget to enable OAuth in SmartApp IDE settings")
            DEBUG(ex)
		}
	}
}

private def DEBUG(message){
	log.debug message
}