/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.Settings;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.Optional;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public abstract class MusicCommand extends Command {

    protected final Bot bot;
    protected boolean bePlaying;
    protected boolean beListening;
    
    public MusicCommand(Bot bot)
    {
        this.bot = bot;
        this.guildOnly = true;
        this.category = bot.MUSIC;
    }
    
    @Override
    protected void execute(CommandEvent event) {
        Settings settings = bot.getSettings(event.getGuild());
        TextChannel tchannel = event.getGuild().getTextChannelById(settings.getTextId());
        if(tchannel!=null && !event.getTextChannel().equals(tchannel))
        {
            try {
                event.getMessage().delete().queue();
            } catch(PermissionException e){}
            event.replyInDm(event.getClient().getError()+" You can only use that command in <#"+settings.getTextId()+">!");
            return;
        }
        if(bePlaying
                && (event.getGuild().getAudioManager().getSendingHandler()==null
                || !((AudioHandler)event.getGuild().getAudioManager().getSendingHandler()).isMusicPlaying()))
        {
            event.reply(event.getClient().getError()+" There must be music playing to use that!");
            return;
        }
        if(beListening)
        {
            VoiceChannel current = event.getGuild().getSelfMember().getVoiceState().getChannel();
            if(current==null)
                current = event.getGuild().getVoiceChannelById(settings.getVoiceId());
            GuildVoiceState userState = event.getMember().getVoiceState();
            if(!userState.inVoiceChannel() || userState.isDeafened() || (current!=null && !userState.getChannel().equals(current)))
            {
                event.reply(event.getClient().getError()
                        +" You must be listening in "+(current==null ? "a voice channel" : "**"+current.getName()+"**")
                        +" to use that!");
                return;
            }
            if(!event.getGuild().getSelfMember().getVoiceState().inVoiceChannel())
                try {
                    if (settings.getAvoidOtherBots()) {
                        Optional<Member> otherBot = userState.getChannel().getMembers().stream().filter(m -> m.getUser().isBot()).findFirst();
                        if (otherBot.isPresent()) {
                            event.reply(event.getClient().getError() + " I am unable to connect to **"+userState.getChannel().getName()+"**; another bot detected");
                            return;
                        }
                    }
                    event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                }catch(PermissionException ex) {
                    event.reply(event.getClient().getError()+" I am unable to connect to **"+userState.getChannel().getName()+"**!");
                    return;
                }
        }
        doCommand(event);
    }
    
    public abstract void doCommand(CommandEvent event);
}
