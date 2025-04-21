package com.spag.gatelogger.server.access_control;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.spag.lua.LuaObject;
import com.spag.lua.LuaOptional;
import com.spag.lua.LuaString;
import com.spag.lua.LuaTable;
import com.spag.gatelogger.server.App;

public class User {
  static Path usersPath = App.dataPath.resolve("users");

  public static User get(LuaString username, LuaObject passHash) throws IllegalAccessException {
    File userFile = usersPath.resolve(username.value + ".user").toFile();
    if (userFile.exists()) {
      return new User(userFile, passHash);
    }
    throw new IllegalAccessError("User \"%s\" does not exist");
  }

  @SuppressWarnings("unused")
  private Optional<String> passwordHash;
  private LuaTable perms;

  private User(File userFile, LuaObject passHash) throws IllegalAccessException {
    try {
      LuaTable userData = LuaTable.fromString(Files.readString(usersPath));
      Optional<LuaObject> expectedPassword = LuaOptional.ofNilable(userData.get(LuaString.of("password")));
      if (expectedPassword.map(pass -> pass == passHash).orElse(true)) {
        this.perms = (LuaTable) LuaOptional.ofNilable(userData.get(LuaString.of("perms")))
            .orElse(new LuaTable());
        this.passwordHash = expectedPassword.map(p -> ((LuaString) p).value);
      } else {
        throw new IllegalAccessException("Password does not match");
      }
    } catch (IOException IOE) {
      throw new Error(IOE);
    }
  }

  public boolean reAuth(String passwordHash) {
    return this.passwordHash.map(curr -> curr.equals(passwordHash)).orElse(false);
  }

  public int getPerm(String gateID) {
    final LuaTable defaultPerms = (LuaTable) LuaOptional.ofNilable(perms.get(LuaString.of("gateDefault")))
        .orElse(new LuaTable());
    LuaTable gatePerms = (LuaTable) LuaOptional.ofNilable(perms.get(LuaString.of("gateSpecific")))
        .map(t -> (LuaTable) t).flatMap(t -> LuaOptional.ofNilable(t.get(LuaString.of(gateID))))
        .orElse(defaultPerms);
    int perms = Permission.of(gatePerms);
    return this.passwordHash.isPresent() ? perms : perms & (Permission.MAX ^ Permission.PASSWORD_REQUIRED);
  }
}
