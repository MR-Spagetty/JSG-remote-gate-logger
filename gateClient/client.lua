local event = require "event"
local serial = require "serialization"
local component = require "component"
if not component.isAvailable("internet") then
  print("This program requires an internet card to run.")
  os.exit()
end
local internet = component.internet
local sg = component.stargate
if component.isAvailable "modem" then
  for port = 1, 16 do
    component.modem.open(port)
  end
end
local conf = require "config"

local eventQueue = {}
local con


local function getCon()
  local conGot;
  local res
  local attempt = 0
  while not res do
    print("Attempting socket connection " .. attempt)
    if conf.port ~= nil then
      conGot = internet.connect(conf.address, conf.port)
    else
      conGot = internet.connect(conf.address)
    end
    os.sleep()
    conGot.finishConnect()
    res = conGot.finishConnect()
    attempt = attempt + 1
    if attempt > 5 then
      print("Failed to connect to the server after 5 attempts.")
      os.exit()
    end
  end
  conGot.write(serial.serialize({
    os.date(),
    {
      "init",
      name = conf.name,
      status = sg.getGateStatus(),
      dialed = sg.dialedAddress,
      hasDHD = component.isAvailable "dhd"
    }
  }))
  return conGot
end

local function sendEvent(e)
  table.insert(eventQueue, e)
  local res = con.write(serial.serialize(eventQueue))
  if res == nil then
    return
  elseif res > 0 then
    eventQueue = {}
  end
end

local function processEvent(e)
  if e[1]:sub(1, #"stargate") == "stargate" then
    return {
      os.date(),
      id = sg.address,
      type = "stargate",
      data = e
    }
  elseif e[1] == "modem_message" then
    return {
      os.date(),
      id = sg.address,
      type = "modem",
      data = e
    }
  else
    return {
      os.date(),
      id = sg.address,
      type = "other",
      data = e
    }
  end
end

local function receiveCommand()
  local data = con.read()
  local command = {}
  if data == nil then
    return nil
  elseif data ~= "" then
    command = serial.unserialize(data)
  end
  return command
end

local function execute(command)
  print("Executing command: " .. command[1])
  if command[1] == "update" then
    os.execute(
      "wget https://raw.githubusercontent.com/MR-Spagetty/JSG-remote-gate-logger/refs/heads/main/gateClient/client.lua /home/client.lua -f")
    os.execute(
      "wget https://raw.githubusercontent.com/MR-Spagetty/JSG-remote-gate-logger/refs/heads/main/gateClient/.shrc /home/.shrc -f")
    os.execute("reboot")
  elseif command[1] == "status" then
    con.write(serial.serialize { os.date(), id = sg.address,
      data = {
        "status",
        status = sg.getGateStatus(),
        dialed = sg.dialedAddress,
        hasDHD = component.isAvailable "dhd"
      }
    })
  elseif command[1] == "stop" then
    sendEvent({ os.date(), id = sg.address, type = "bye bye" })
    con.close()
    os.exit()
  else
    print("Unknown command: " .. command[1])
    sendEvent { os.date(), id = sg.address, data = { "error", "Unknown command" } }
  end
end

local function main()
  con = getCon()
  while true do
    local command = receiveCommand()
    if command ~= nil then
      execute(command)
      local event = { event.pull(0.1) }
      if event[1] then
        sendEvent(processEvent(event))
      end
    end
  end
end

main()
