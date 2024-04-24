import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Event extends ListenerAdapter {
	private Guild guild;
	private MessageChannel log, emoji;
	private long timer = 1;
	/* this My discord Server ID, log channel ID and emoji channel ID */
	/* target Guild ID HERE */
	private String guild_c = "1199195857166282782";
	/* target Log TextChannel ID HERE */
	private String log_c = "1204283334633590814";
	/* target Emoji TextChannel ID HERE */
	private String emoji_c = "1204311113685008394";
	
	public Event(JDA jda) {
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
			guild.getEmojis().forEach((emo) -> {
				emo.delete().queueAfter(timer++, TimeUnit.SECONDS);
			});
			timer = 1;
		}, WeekTime(DayOfWeek.SATURDAY), 7L * 24L * 60L * 60L * 1000L, TimeUnit.MILLISECONDS);
		
		Executors.newScheduledThreadPool(1).schedule(() -> {
			/* target Guild ID HERE */
			guild = jda.getGuildById(guild_c);
			/* target Log TextChannel ID HERE */
			log = guild.getTextChannelById(log_c);
			/* target Emoji TextChannel ID HERE */
			emoji = guild.getTextChannelById(emoji_c);
		}, 1, TimeUnit.MINUTES);
		
	}	
		
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		event.deferReply(true).queue();
		
		if(event.getFullCommandName().equals("emoji")) {
			if(event.getOption("delete") != null) {
				guild.getEmojis().forEach((emoji) -> {
					emoji.delete().delay(timer++, TimeUnit.SECONDS).queue();
				});
				event.getHook().sendMessage("모든 이모티콘을 성공적으로 제거하였습니다 ^+^").setEphemeral(true).queueAfter(timer, TimeUnit.SECONDS);
				timer = 1;
			}
			else if(event.getOption("add") != null) {
				event.getHook().sendMessage("기존 채널이 아니라 이번 신규 채널에 봇이 보낸 해시값과 문자열로 이모티콘을 제작합니다.").setEphemeral(true).queue();
			}
			else {
				event.getHook().sendMessage("기존 채널이 아니라 이번 신규 채널에 이모티콘 삭제 및 생성 로그를 찍습니다.").setEphemeral(true).queue();
			}
		}
		else {
			event.getChannel().getHistory().retrievePast(50).queue(targetList -> {
				targetList.forEach(target -> {
					System.err.println(timer);
					if(!target.isEphemeral() && !target.getType().equals(MessageType.UNKNOWN) && !target.isWebhookMessage())
						event.getChannel().deleteMessageById(target.getId()).delay(timer++, TimeUnit.SECONDS).queue();
				});
			});
			event.getHook().sendMessage("이 채널의 최근 메세지 약 50개의 메세지를 제거하였습니다.").setEphemeral(true).queueAfter(timer, TimeUnit.SECONDS);
			timer = 1;
		}
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.isFromGuild() && event.getChannel().equals(emoji) && event.getAuthor().isBot()) {
			String[] input = event.getMessage().getContentRaw().split(" ");
			String name = input[0];
			String link = input[1];
			
			try {
				URL url = new URL("https://www.bungie.net" + link);
				guild.createEmoji(name, Icon.from(url.openStream().readAllBytes())).queue();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			if(event.getMessage().getContentRaw().toLowerCase().equals("!exit")) {
				event.getJDA().shutdownNow();
				System.exit(0);
			}
		}
	}
	
	@Override
	public void onEmojiAdded(EmojiAddedEvent event) {
		log.sendMessage(event.getEmoji().getAsMention() + "을 서버에 추가하였습니다.").queue();
	}
	
	@Override
	public void onEmojiRemoved(EmojiRemovedEvent event) {
		log.sendMessage(event.getEmoji().getAsMention() + "을 서버에서 제거하였습니다.").queue();
	}

	private long WeekTime(DayOfWeek day) {
		LocalDateTime target = LocalDateTime
				.now().with(TemporalAdjusters.nextOrSame(day))
				.withHour(1)
				.withMinute(50)
				.withSecond(0)
				.withNano(0);
		LocalDateTime curr = LocalDateTime.now();
		long res = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
				- curr.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		return res < 0 ? res + (7L * 24L * 60L * 60L * 1000) : res;
	}
}
