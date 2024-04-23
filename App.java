import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class App {
	public static void main(String[] args) {
		/*
		 * Discord Bot API TOKEN HERE.
		 * Don't show it to anyone else.
		*/
		String Token = "API TOKEN HERE";
		JDA jda = JDABuilder.create(Token, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES)
				.setActivity(Activity.customStatus("언제든 이벤트를 처리할 준비가 되었따!"))
				.build();
		jda.addEventListener(new Event(jda));
		addCommand(jda);
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> {
			if(jda.getStatus().equals(Status.SHUTDOWN)) {
				System.out.println("정상적인 종료가 되었습니다.");
				scheduler.shutdown();
				System.exit(0);
			}
		}, 0, 2, TimeUnit.SECONDS);
	}
	
	private static void addCommand(JDA jda) {
		jda.updateCommands().addCommands(
				Commands.slash("emoji", "이모티콘을 설정합니다.")
					.addOption(OptionType.STRING, "delete", "서버에 있는 모든 이모티콘을 삭제합니다. 순차적으로 제거되므로 오래걸릴 수 있습니다.", false)
					.addOption(OptionType.STRING, "add", "입력된 이모티콘을 추가하는 채널을 지정합니다.", false)
					.addOption(OptionType.STRING, "log", "이모티콘을 추가하거나 삭제한 기록을 출력합니다.", false),
				Commands.slash("remove", "이 채널에 존재하는 모든 메세지를 제거합니다. 순차적으로 제거되므로 오래걸릴 수 있습니다.")
			).queue();	
	}
}