package Discord;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class App {
	public static void main(String[] args) {
		String Token = "Your-Discord-BOT-Token-HERE";
		JDA jda = JDABuilder
				.create(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
				.setToken(Token)
				.setActivity(Activity.customStatus("인수 인계 받는중......."))
				.build();
		
		jda.addEventListener(new Event(jda));
		addCommand(jda);
		updateActivity(jda);
		
	}
	
	private static void addCommand(JDA jda) {
		jda.updateCommands().addCommands(
				Commands.slash("xur", "주간에 쥴이 가져오는 아이템을 포스팅해요! (매주 토요일 2시 이후) (DM or Server)")
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
				Commands.slash("currxur", "이번주(혹은 저번주) 쥴이 가져오는 아이템을 보여줘요! (DM or Server)"),
				Commands.slash("disable", "쥴이 포스팅되는 채널을 비활성화 하거나, 개인 메세지(DM)을 보낼 수 없게 만들어요. (DM or Server)")
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
				).queue();
	}
	 
	private static long getTime() {
		LocalDateTime now = LocalDateTime.now();
		if(now.getSecond() != 0 || now.getMinute() != 0) {
			LocalDateTime target = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
			return target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() 
					- now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		}
		return 0L;
	}
	
	private static void updateActivity(JDA jda) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> {
			LocalDateTime now = LocalDateTime.now();
			if(now.getDayOfWeek() == DayOfWeek.SATURDAY) {
				if(0 <= now.getHour() && now.getHour() <= 2)
					jda.getPresence().setActivity(Activity.customStatus("야근중....(2...)"));
				else
					jda.getPresence().setActivity(Activity.customStatus("피곤해서 진짜 자는중...."));
			}
			else if(now.getDayOfWeek() == DayOfWeek.FRIDAY) {
				if(0 <= now.getHour() && now.getHour() <= 6)
					jda.getPresence().setActivity(Activity.customStatus("자는중 Zzz...."));
				else if(7 <= now.getHour() && now.getHour() <= 11)
					jda.getPresence().setActivity(Activity.customStatus("오전 업무 처리중...."));
				else if(12 <= now.getHour() && now.getHour() <= 13)
					jda.getPresence().setActivity(Activity.customStatus("점심시간 즐기는중"));
				else if(14 <= now.getHour() && now.getHour() <= 16)
					jda.getPresence().setActivity(Activity.customStatus("오후 업무 처리중...."));
				else if(17 == now.getHour())
					jda.getPresence().setActivity(Activity.customStatus("저녁 식사중...."));
				else
					jda.getPresence().setActivity(Activity.customStatus("야근중...."));
			}
			else {
				if(0 <= now.getHour() && now.getHour() <= 6)
					jda.getPresence().setActivity(Activity.customStatus("자는중 Zzz...."));
				else if(7 <= now.getHour() && now.getHour() <= 11)
					jda.getPresence().setActivity(Activity.customStatus("오전 업무 처리중...."));
				else if(12 <= now.getHour() && now.getHour() <= 13)
					jda.getPresence().setActivity(Activity.customStatus("점심시간 즐기는중"));
				else if(14 <= now.getHour() && now.getHour() <= 16)
					jda.getPresence().setActivity(Activity.customStatus("오후 업무 처리중...."));
				else if(17 == now.getHour())
					jda.getPresence().setActivity(Activity.customStatus("퇴근중"));
				else if(18 <= now.getHour() && now.getHour() <= 21)
					jda.getPresence().setActivity(Activity.customStatus("히히 데가중 ㅎㅎ"));
				else
					jda.getPresence().setActivity(Activity.customStatus("자는중 Zzz...."));
			}
		}, getTime(), 60L * 60L * 1000L, TimeUnit.MILLISECONDS);
	}
}