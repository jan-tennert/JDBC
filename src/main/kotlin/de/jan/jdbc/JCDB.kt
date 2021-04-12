package de.jan.jdbc

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class JCDB(val jda: JDA) : ListenerAdapter() {

    private val commands = mutableListOf<Command>()

    init {
        jda.addEventListener(this)
    }

    override fun onSlashCommand(event: SlashCommandEvent) {
        for (command in commands) {
            if(event.name == command.name) {
                if(event.member != null) {
                    val missingPermissions = mutableListOf<Permission>()
                    for (permission in command.permissions) {
                        if(!event.member!!.hasPermission(permission)) missingPermissions.add(permission)
                    }
                    val missingBotPermissions = mutableListOf<Permission>()
                    for (botPermission in command.botPermissions) {
                        if(!event.guild!!.selfMember.hasPermission(botPermission)) missingPermissions.add(botPermission)
                    }
                    if(missingPermissions.isNotEmpty()) {
                        event.reply("Du brauchst diese Berechtigungen, um diesen Befehl nutzen zu können: ${missingPermissions.toFormattedString()}").queue()
                        return
                    }
                    if(missingBotPermissions.isNotEmpty()) {
                        event.reply("Der Bot braucht diese Berechtigungen, für diesen Befehl: ${missingBotPermissions.toFormattedString()}").queue()
                        return
                    }
                    command.run(if(event.isFromGuild) event.textChannel else null, event.member, event.user, if(!event.isFromGuild) event.privateChannel else null, event.hook, event.options, event)
                } else {
                    command.run(if(event.isFromGuild) event.textChannel else null, event.member, event.user, if(!event.isFromGuild) event.privateChannel else null, event.hook, event.options, event)
                }
            }
        }
    }

    fun registerCommands(vararg commands: Command) {
        this.commands.addAll(commands)
        var index = 0
        for (command in commands) {
            index++
            println("[CommandHandler] Registering Command $index/${commands.size}: ${command.name}")
            if(command.guildOnly.first) {
                jda.getGuildById(command.guildOnly.second)!!.updateCommands().addCommands(command).queue()
            } else {
                jda.updateCommands().addCommands(command).queue()
            }
        }
        println("[CommandHandler] Registered all commands!")
    }
}

fun <T>Collection<T>.toFormattedString(separator: String = ", ") : String {

    var string = ""
    for ((index, t) in this.withIndex()) {
        if(index != this.size - 1) {
            string += t.toString() + separator
        } else {
            string += t.toString()
        }
    }
    return string
}