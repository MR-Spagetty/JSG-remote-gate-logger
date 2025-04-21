package com.spag.gatelogger.server.access_control;

import java.math.BigDecimal;

import com.spag.lua.LuaBool;
import com.spag.lua.LuaNum;
import com.spag.lua.LuaObject;
import com.spag.lua.LuaString;
import com.spag.lua.LuaTable;

public class Permission {

  public static int CAN_VIEW = 1;
  public static int ADDRESS_VIEW = 2;
  public static int DIAL = 4;
  public static int DHD = 8;
  public static int IRIS_VIEW = 16;
  public static int IDC_VIEW = 32;
  public static int IRIS_CONTROL = 64;
  public static int SHELL = 128;
  public static int MAX = CAN_VIEW | ADDRESS_VIEW | DIAL | DHD | IDC_VIEW | IRIS_CONTROL | SHELL;
  public static int PASSWORD_REQUIRED = SHELL | IRIS_CONTROL | IDC_VIEW;

  public static int of(LuaTable gatePerms) {
    LuaObject permMaskSet = gatePerms.get(LuaString.of("permMask"));
    if (permMaskSet instanceof LuaNum permMask) {
      return permMask.value.intValue();
    }
    int out = 0;
    if (truthy(gatePerms.get(LuaString.of("viewAddress")))) {
      out |= ADDRESS_VIEW;
    }
    if (truthy(gatePerms.get(LuaString.of("canDial")))) {
      out |= DIAL;
    }
    if (truthy(gatePerms.get(LuaString.of("allowDHD")))) {
      out |= DIAL;
      out |= DHD;
    }
    if (truthy(gatePerms.get(LuaString.of("viewIDC")))) {
      out |= IDC_VIEW;
    }
    if (truthy(gatePerms.get(LuaString.of("irisControl")))) {
      out |= IRIS_CONTROL;
    }
    if (truthy(gatePerms.get(LuaString.of("allowShell")))) {
      out |= SHELL;
    }
    if (out > CAN_VIEW || truthy(gatePerms.get(LuaString.of("view")))) {
      out |= CAN_VIEW;
    }
    return out;
  }

  private static boolean truthy(LuaObject value) {
    return switch (value) {
      case LuaNum ln -> ln.value.compareTo(BigDecimal.ZERO) > 0;
      case LuaBool lb -> lb.get();
      default -> false;
    };
  }

  public static boolean has(int requiredPermission, int permissions) {
    return (permissions & requiredPermission) == requiredPermission;
  }
}
