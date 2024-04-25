package Discord;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Utils.Bungie;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;

@SuppressWarnings("unchecked")
public class Event extends ListenerAdapter{
	private String access = "Your Access-Key-URL-HERE";
	private String refresh = "Your Refresh-Key-URL-HERE";
	private String log = "Your Log-URL-HERE";
	private String clanId = "1199195857166282782";
	private String chanId = "1204311113685008394";
	
	private JDA jda = null;
	private EmbedBuilder embed = null;
	private Bungie bungie = null;
	
	private Map<String, Object> poster = null;
	private Map<Guild, MessageChannel> AllGuild = new HashMap<>();
	private Set<User> AllUser = new HashSet<>();
	
	private String[][] image = {
			{
				"https://destiny.wiki.gallery/images/thumb/a/a9/Destiny_tower_screenshot.jpg/1280px-Destiny_tower_screenshot.jpg",
				"https://www.bungie.net/img/destiny_content/pgcr/patrol_edz.jpg",
				"https://www.bungie.net/img/destiny_content/pgcr/patrol_nessus.jpg"
			}, 
			{
				"탑 - 격납고",
				"EDZ - 구불구불한 만",
				"네소스 - 감시자의 무덤"
			}
	};
	
	/**
	 * event 객체의 생성자를 이용하면 자동으로 embed의 생성 및 클랜과 DM으로 돌아오는 주의 토요일날 알아서 보내줌
	 * 이때, 스케쥴링은 절때 취소할 수 없으며, final한, 변동이 없을 예정임.
	 */	
	public Event(JDA jda) {
		this.jda = jda;
		bungie = new Bungie(access, refresh, log);
		init();
		setGuild("", false);
		setUser("", false);
	}
	
	private void setGuild(String str, boolean check) {
		try {
			File guild = new File("Your-Clan-File-HERE");
			if(str.isEmpty()) {
				BufferedReader br = new BufferedReader(new FileReader(guild));
				String input;
				
				while((input = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(input);
					Guild g = jda.getGuildById(st.nextToken());
					TextChannel c = jda.getTextChannelById(st.nextToken());
					AllGuild.put(g, c);
				}
				br.close();
			}
			else if(check) {
				File tmp = new File("Your-Clan-TMPFile-HERE");
				BufferedWriter bw = new BufferedWriter(new FileWriter(tmp));
				BufferedReader br = new BufferedReader(new FileReader(guild));
				String input;
				
				while((input = br.readLine()) != null) {
					if(!input.equals(str)) {
						bw.write(input);
						bw.newLine();
					}
				}
				bw.flush();
				bw.close();
				br.close();
				
				if(guild.delete()) {
					if(!tmp.renameTo(guild)) {
						System.err.println("Error");
					}
				}
			}
			else {
				String[] info = str.split(" ");
				Guild g = jda.getGuildById(info[0]);
				TextChannel c = jda.getTextChannelById(info[1]);
				if(AllGuild.putIfAbsent(g, c) == null) {
					BufferedWriter bw = new BufferedWriter(new FileWriter(guild, true));
					bw.write(str);
					bw.newLine();
					bw.flush();
					bw.close();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setUser(String str, boolean check) {
		try {
			File user = new File("Your-User-File-HERE");
			if(str.isEmpty()) {
				BufferedReader br = new BufferedReader(new FileReader(user));
				String input;
				
				while((input = br.readLine()) != null) {
					User u = jda.getUserById(input);
					AllUser.add(u);
				}
				br.close();
			}
			else if(check) {
				File tmp = new File("Your-User-TMPFile-HERE");
				BufferedWriter bw = new BufferedWriter(new FileWriter(tmp));
				BufferedReader br = new BufferedReader(new FileReader(user));
				String input;
				
				while((input = br.readLine()) != null) {
					if(!input.equals(str)) {
						bw.write(input);
						bw.newLine();
					}
				}
				bw.flush();
				bw.close();
				br.close();
				
				if(user.delete()) {
					if(!tmp.renameTo(user)) {
						System.err.println("Error");
					}
				}
			}
			else {
				User u = jda.getUserById(str);
				if(AllUser.add(u)) {
					BufferedWriter bw = new BufferedWriter(new FileWriter(user, true));
					bw.write(str);
					bw.newLine();
					bw.flush();
					bw.close();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void init() {
		long week = 7L * 24L * 60L * 60L * 1000L;
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
		/*
		 * 현재 시간이 매주 토요일 2시 ~ 수요일 2시 이전 이라면.
		 */
		if(ableXur(LocalDateTime.now())) {
			poster = bungie.xur();
			Timer task = new Timer();
			task.schedule(new TimerTask() {

				@Override
				public void run() {
					sendMessage((Map<String, Object>) poster.get("item"));
				}
					
			}, 1000L * 30L);

			Timer task2 = new Timer();
			task2.schedule(new TimerTask(){

				@Override
				public void run() {
					Thread thread = new Thread(buildEmbed);
					thread.start();
					jda.getPresence().setActivity(Activity.customStatus("done."));
				}

			}, 1000L * 60L * 3L);
			
		}
		scheduler.scheduleAtFixedRate(() -> {
			poster = bungie.xur();
			Timer task = new Timer();
			task.schedule(new TimerTask() {

				@Override
				public void run() {
					sendMessage((Map<String, Object>) poster.get("item"));
					task.cancel();
				}
					
			}, 1000L * 30L);
		}, WeekTime(DayOfWeek.SATURDAY) + (30L * 1000L), week, TimeUnit.MILLISECONDS);
		scheduler.scheduleAtFixedRate(buildEmbed, WeekTime(DayOfWeek.SATURDAY) + (2L * 60L * 1000L), week, TimeUnit.MILLISECONDS);
		scheduler.scheduleAtFixedRate(sendGuild, WeekTime(DayOfWeek.SATURDAY) + (4L * 60L * 1000L), week, TimeUnit.MILLISECONDS);
		scheduler.scheduleAtFixedRate(sendDM, WeekTime(DayOfWeek.SATURDAY) + (4L * 60L * 1000L), week, TimeUnit.MILLISECONDS);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.isFromGuild()) return;
		if(event.getMessage().getContentRaw().toUpperCase().equals("!EXIT")) {
			if(event.getAuthor().getId().equals("407024356712251406")) {
				event.getJDA().shutdownNow();
				System.exit(0);
			}
		}
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		event.deferReply(true).queue();
		LocalDateTime now = LocalDateTime.now();
		
		if(event.getFullCommandName().equals("xur")) {
			if(event.isFromGuild()) {
				if(AllGuild.getOrDefault(event.getGuild(), null) == null) {
					setGuild(event.getGuild().getId() + " " + event.getChannelId(), false);
				}
				else if(!AllGuild.get(event.getGuild()).equals(event.getChannel())) {
					event.getHook().sendMessage("이미 이 서버의 다른 채널에서 쥴이 매주 화요일마다 포스팅을 진행해줄거에요! 바로 확인하는 명령어는 \"/currxur\"을 사용해주세요!").setEphemeral(true).queue();
					return;
				}
				else {
					event.getHook().sendMessage("이미 쥴이 포스팅할 채널이 지정되어 있습니다. (채널: " + AllGuild.get(event.getGuild()).getName() + ") 이 채널을 다음과 같은 명령어로 삭제해주세요. \"/disable\"").setEphemeral(true).queue();
					return;
				}
				
				if(ableXur(now)) {
					if(embed == null) {
						Timer task1 = new Timer();
						task1.schedule(new TimerTask() {

							@Override
							public void run() {
								Thread t = new Thread(buildEmbed);
								t.start();	
								task1.cancel();
							}
							
						}, 1000L * 60L);
												
						Timer task2 = new Timer();
						task2.schedule(new TimerTask() {

							@Override
							public void run() {
								event.getHook().deleteOriginal().queue();
								event.getChannel().sendMessageEmbeds(embed.build()).queue();
								task2.cancel();
							}
							
						}, 1000L * 60L + 10000L);
					}
					else {
						event.getHook().deleteOriginal().queue();
						event.getChannel().sendMessageEmbeds(embed.build()).queue();
					}
				}
				else if(embed != null && !ableXur(now)) {
					embed.setDescription("저번주 암상인이 가져온 아이템 목록입니다.");
					event.getHook().deleteOriginal().queue();
					event.getChannel().sendMessageEmbeds(embed.build()).queue();
				}
				else {
					event.getHook().sendMessage("이 채널을 이번주 토요일날 포스팅이 될 수 있도록, 매주 토요일 2시 30분 이전에 포스팅이 될 수 있도록 지정했어요!").setEphemeral(true).queue();
				}
			}
			else {
				if(!AllUser.contains(event.getUser())) {
					setUser(event.getUser().getId(), false);
				}
				else if(AllGuild.containsKey(event.getGuild())) {
					event.getHook().sendMessage("이미 DM으로 쥴이 매주 화요일마다 포스팅을 진행해줄거에요! 바로 확인하는 명령어는 \"/currxur\"을 사용해주세요!").setEphemeral(true).queue();
					return;
				}
				else {
					event.getHook().sendMessage("기존에 쥴의 포스팅 설정이 되어있습니다. 포스팅을 그만 받길 원하시면 이런 명령어를 사용해주세요. \"/disable\"").setEphemeral(true).queue();
					return;
				}
				
				if(ableXur(now)) {
					if(embed == null) {
						Timer task1 = new Timer();
						task1.schedule(new TimerTask() {

							@Override
							public void run() {
								Thread t = new Thread(buildEmbed);
								t.start();	
								task1.cancel();
							}
							
						}, 1000L * 60L);
												
						Timer task2 = new Timer();
						task2.schedule(new TimerTask() {

							@Override
							public void run() {
								event.getHook().deleteOriginal().queue();
								event.getChannel().sendMessageEmbeds(embed.build()).queue();
								task2.cancel();
							}
							
						}, 1000L * 60L + 10000L);
					}
					else if(embed != null && !ableXur(now)) {
						embed.setDescription("저번주 암상인이 가져온 아이템 목록입니다.");
						event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
					}
					else {
						event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
					}
				}
				else {
					event.getHook().sendMessage("이 채널을 이번주 토요일날 포스팅이 될 수 있도록, 매주 토요일 2시 30분 이전에 포스팅이 될 수 있도록 지정했어요!").setEphemeral(true).queue();
				}
			}
		}
		else if(event.getFullCommandName().equals("disable")) {
			if(event.isFromGuild()) {
				if(AllGuild.remove(event.getGuild()) != null) {
					event.getHook().sendMessage("쥴이 이 채널에서 포스터를 그만뒀어요.").setEphemeral(true).queue();
					setGuild(event.getGuild().getId() + " " + event.getChannelId(), true);
				}
				else {
					event.getHook().sendMessage("이 채널은 쥴의 포스팅 설정이 되어있지 않거나, 쥴이 접근할 수 없는 채널이에요.").setEphemeral(true).queue();
				}
			}
			else {
				if(AllUser.remove(event.getUser())) {
					event.getHook().sendMessage("쥴이 이 채널에서 포스터를 그만뒀어요.").setEphemeral(true).queue();
					setUser(event.getUser().getId(), true);
				}
				else {
					event.getHook().sendMessage("이 DM은 쥴의 포스팅 설정이 되어있지 않거나, 쥴이 접근할 수 없는 채널이에요.").setEphemeral(true).queue();
				}
			}
		}
		else if(event.getFullCommandName().equals("currxur")) {
			if(ableXur(now)) {
				if(embed == null) event.getHook().sendMessage("잠시만 기다려주세요.\n금방 쥴이 올릴 물건을 만들고있어요.").setEphemeral(true).queue();
				else {
					event.getHook().deleteOriginal().queue();
					event.getChannel().sendMessageEmbeds(embed.build()).queue();
				}
			}
			else if(embed != null && !ableXur(now)) {
				embed.setDescription("저번주 암상인이 가져온 아이템 목록입니다.");
				event.getHook().deleteOriginal().queue();
				event.getChannel().sendMessageEmbeds(embed.build()).queue();
			}
		}
	}
	
	private void sendMessage(Map<String, Object> map) {
		Guild guild = jda.getGuildById(clanId);
		TextChannel emoji = guild.getTextChannelById(chanId);
		for(Map.Entry<String, Object> ent1 : map.entrySet()) {
			if(ent1.getValue() instanceof Map) {
				for(Map.Entry<String, Object> ent2 : ((Map<String, Object>) ent1.getValue()).entrySet()) {
					if(ent2.getValue() instanceof Map) {
						String hash = null, icon = null;
						for(Map.Entry<String, Object> ent3 : ((Map<String, Object>) ent2.getValue()).entrySet()) {
							if(ent3.getKey().equals("name")) continue;
							else if(ent3.getKey().equals("itemHash")) hash = String.valueOf(ent3.getValue());
							else icon = String.valueOf(ent3.getValue());
						}
						if(guild.getEmojisByName(hash, true).isEmpty()) {
							emoji.sendMessage(hash + " " + icon).queue();
						}
					}
				}
			}
		}
	}
	
	// 토요일 오전 2시 10분 이후 ~ 돌아오는 화요일 11시 10분 이전
	private boolean ableXur(LocalDateTime time) {
		DayOfWeek currDay = time.getDayOfWeek();
		LocalTime currTime = time.toLocalTime();
		return currDay == DayOfWeek.SATURDAY && currTime.isAfter(LocalTime.of(2, 0))
				|| currDay == DayOfWeek.SUNDAY 
				|| currDay == DayOfWeek.MONDAY
				|| currDay == DayOfWeek.TUESDAY && currTime.isBefore(LocalTime.of(23, 10));	
	}
	
	private long WeekTime(DayOfWeek day) {
		LocalDateTime target = LocalDateTime
				.now().with(TemporalAdjusters.nextOrSame(day))
				.withHour(2)
				.withMinute(0)
				.withSecond(0)
				.withNano(0);
		LocalDateTime curr = LocalDateTime.now();
		long res = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
				- curr.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		return res < 0 ? res + (7L * 24L * 60L * 60L * 1000) : res;
	}
	
	private String getEmoji(String name) {
		Guild guild = jda.getGuildById(clanId);
		for(RichCustomEmoji emoji : guild.getEmojis()) {
			if(emoji.getName().equals(name)) {
				return emoji.getAsMention();
			}
		}
		return null;
	}
		
	private Runnable buildEmbed = () -> {
		/* 이 시점에는 이미 클랜에 이모티콘이 추가되어있고, 이후에 접근도 가능한 그 시점이어야함. 안되면 위쪽에 딜레이를 더 주는 방식으로 선택해야할듯. */
		embed = new EmbedBuilder();
		embed.setAuthor("Xûr", null, "https://www.bungie.net/img/destiny_content/vendor/icons/xur_map_icon.png");
		embed.setThumbnail("https://pkmanager.twingalaxies.com/assets/article/2019/06/21/xur-gray_feature.png");
		embed.setColor(new Color(191, 255, 0));
		embed.setTitle("아홉의 요원. xûr");
		embed.setDescription("이번주 암상인이 가져온 아이템 목록입니다.");
				
		String date = String.valueOf(((Map<String, Object>) poster.get("xur")).get("disableTime"));
		int location = (int) ((Map<String, Object>) poster.get("xur")).get("locationIndex");
		embed.addField("Reset", date, false);
		
		for(Map.Entry<String, Object> ent1 : ((Map<String, Object>) poster.get("item")).entrySet()) {
			StringBuilder sb = new StringBuilder();
			for(Map.Entry<String, Object> ent2 : ((Map<String, Object>) ent1.getValue()).entrySet()) {
				String emoji = null, name = null;
				for(Map.Entry<String, Object> ent3 : ((Map<String, Object>) ent2.getValue()).entrySet()) {
					if(ent3.getKey().equals("name")) name = String.valueOf(ent3.getValue());
					else if(ent3.getKey().equals("itemHash")) emoji = getEmoji(String.valueOf(ent3.getValue()));
					else continue;
				}
				sb.append(emoji).append(" ").append(name).append("\n");
			}
			embed.addField(ent1.getKey(), sb.toString(), false);
		}
		
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, Object> entry : ((Map<String, Object>) poster.get("armor")).entrySet()) {
			sb.append(entry.getKey()).append(": ").append(String.valueOf(entry.getValue())).append("\n");
		}
		embed.addField("방어구 세트", sb.toString(), false);
		embed.addField("위치", image[1][location], true);
		embed.setImage(image[0][location]);
		embed.setFooter("태초마을", "https://www.bungie.net/common/destiny2_content/icons/17f662752d015d6e20e24a4ae474628e.png");
	};
	
	private Runnable sendGuild = () -> {
		if(AllGuild.isEmpty()) return;
		
		MessageEmbed send = embed.build();
		AllGuild.forEach((g, m) -> {
			g.getTextChannelById(m.getId()).sendMessageEmbeds(send).queue(null, new ErrorHandler()
					.ignore(EnumSet.of(ErrorResponse.UNKNOWN_CHANNEL,
							ErrorResponse.CANNOT_SEND_TO_USER,
							ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD,
							ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER)));
		});
	};

	private Runnable sendDM = () -> {
		if(AllUser.isEmpty()) return;
		
		MessageEmbed send = embed.build();
		AllUser.forEach(user -> {
			user.openPrivateChannel().queue(dm -> {
				dm.sendMessageEmbeds(send).queue(null, new ErrorHandler()
						.ignore(EnumSet.of(ErrorResponse.UNKNOWN_CHANNEL,
								ErrorResponse.CANNOT_SEND_TO_USER,
								ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD,
								ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER)));
			});
		});
	};
}