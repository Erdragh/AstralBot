module Minecraft.astralbot.common.main {
  // For the Usage command
  requires transitive jdk.management;

  // For just using Kotlin
  requires transitive kotlin.stdlib;
  requires transitive kotlinx.coroutines.core;

  // For SQL Interaction
  requires transitive java.sql;
  requires exposed.core;
  requires exposed.dao;
  requires exposed.jdbc;

  // For Discord Interaction
  requires net.dv8tion.jda;

  // For Minecraft itself
  requires authlib;
  requires minecraft.merged;
  requires transitive org.slf4j;
  requires brigadier;

  // For accessing the config
  requires forgeconfigapiport.fabric;
  requires java.desktop;
}