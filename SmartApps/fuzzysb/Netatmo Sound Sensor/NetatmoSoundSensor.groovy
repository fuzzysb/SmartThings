/**
 *  Netatmo Sound Sensor Custom Device Attribute Detection
 *
 *  Author: stuart@broadbandtap.co.uk
 *  Date: 29/01/2016
 *
 *  Allows me to utilise a devices custom attribute to set a Virtual Switch for example i use the Sound Sensor on my Netatmo indoor module to turn on a virtual Switch if the noise in db is above a threshold and switch it off when below.
 *  I am using this as an extra detection to see if my main living room is occupied and i will use the switch status along with motion sensors to assess if its correct to turn off lights off.
 *  this is used as i get false positives just from motion as it seems i sit very still sometimes when watching TV. this smartapp is ideal to incorporate your custom attribute into Rule machine for example.
 */

definition(
    name: "Netatmo Sound Level Detection",
    namespace: "fuzzysb	",
    author: "Stuart Buchanan",
    description: "switch on a virtual switch if the value of a custom attribute is above or equals the specified threshold",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
  section("This Netatmo Base Station") {
    input name: "netatmo", type: "device.netatmoBasestation", title: "This Netatmo Base Station"
  }
  
  section("Threshold") {
    input name: "threshold", type: "number", title: "Threshold in db", required: false
  }

  section("This Virtual Switch") {
    input name: "virtualSwitch", type: "capability.switch", title: "switch this virtual switch"
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
  subscribe(netatmo, "noise", handleEvent)
}

def handleEvent(evt) {
  def dbthold = threshold ?: 50
  log.debug "entering handle event method, evaluating against a threshold of ${dbthold}"
  if(netatmo.currentValue("noise").toInteger() >= dbthold.toInteger()){
  	log.debug "evaluated as true"
    log.debug("${netatmo.label ?: netatmo.name} is greater than " + dbthold + " Turning on Virtual Switch")
    virtualSwitch.on()
    log.debug "end of handleEvent"
  }
  else {
    log.debug("${netatmo.label ?: netatmo.name} is below the threshold turning off the Virtual switch.")
	virtualSwitch.off()
  }
}