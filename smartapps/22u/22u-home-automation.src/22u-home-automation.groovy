/**
 *  22u Home Automation
 *
 *  Copyright 2016 Nicholas Curtis
 *
 */
definition(
    name: "22u Home Automation",
    namespace: "22u",
    author: "Nicholas Curtis",
    description: "22u Home Automation system integration with Smart Things hub.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "22u Home Automation", displayLink: "http://home.22u.io"])


preferences {
  section ("Allow 22u to control: ") {
    input "switches", "capability.switch", title: "Lights & Switches", multiple: true, required: true
    input "locks", "capability.lock", title: "Locks", multiple: true, required: true
  }
}

mappings {
    path("/control/:type") {
        action: [
			GET: "getAll"
		]
    }
    path("/control/:type/:id") {
        action: [
        	GET: "get",
            PUT: "put"
		]
    }
}

def put() {
	def type = params.type
    
    if (type == 'switch') {
		return putSwitch(switches, params.id, request.JSON?.state)
    }
    else if (type == 'lock') {
    	return putLock(locks, params.id, request.JSON?.state)
    }
}

def get() {
	def type = params.type
    
    if (type == 'switch') {
    	return getSwitch(switches, params.id)
    }
    else if (type == 'lock') {
    	return getLock(locks, params.id)
    }
}

def getAll() {
    def type = params.type
    
    if (type == 'switch') {
    	return getSwitches(switches)
    }
    else if (type == 'lock') {
    	return getLocks(locks)
    }
}

def putSwitch(allSwitches, switchId, request) {
	def thisSwitch = allSwitches.find {
    	return it.id == switchId
    }
    
    if (request.power) {
    	if (request.brightness != 'none') {
            log.debug "set switch ${switchId} level to ${request.brightness}%"
            thisSwitch.setLevel(request.brightness)
    	}
	    else {
        	log.debug "turning switch ${switchId} on"
            thisSwitch.on()
        }
    } else {
    	log.debug "turning switch ${switchId} off"
        thisSwitch.off()
	}
}

def getSwitch(allSwitches, switchId) {
	return getSwitches(allSwitches).find {
    	return it.id == switchId
    }
}

def getSwitches(allSwitches) {
	def resp = []
    allSwitches.each {
        def brightness = it.currentValue('level')
        def indicator = it.currentValue('indicatorStatus')
        def power = it.currentValue("switch")

        resp << [
        	id: it.id,
            name: it.displayName,
            features: [
        		[type: 'power', supported: true],
                [type: 'brightness', supported: brightness != null],
                [type: 'status', supported: indicator != null],
                [type: 'color', supported: false],
            ],
            state: [
            	power: power == 'on' ? true : false,
	            brightness:brightness != null ? brightness : 'none',
                status: indicator != null ? indicator : 'none',
        	]
		]
    }
    return resp
}

def getLock(allLocks, lockId) {
	return getLocks(allLocks).find {
    	return it.id == lockId
    }
}

def getLocks(allLocks) {
	def resp = []
    allLocks.each {
        resp << [
        	id: it.id,
            name: it.displayName,
            features: [
                [type: 'status', supported: false],
                [type: 'lock', supported: true]
            ],
            state: [
            	lock: it.currentValue('lock') == 'locked' ? true : false
        	]
		]
    }
    return resp
}

def putLock(allLocks, lockId, request) {
	log.debug "putLock ${lockId}"
    def thisLock = allLocks.find {
    	return it.id == lockId
    }
    
    if (request.lock) {
    	log.debug "locking lock ${lockId}"
        thisLock.lock()
    } else {
    	log.debug "unlocking lock ${lockId}"
        thisLock.unlock()
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}