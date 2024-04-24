package Utils;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.Timestamp;

public class Bungie extends Response{
	private String version = "";
	/* your x-api-key here */
	private String x_api_key = "X_API_KEY";
	private String base = "https://www.bungie.net/Platform";
	/* your class profile id here */
	private String[][] Class = {
			{"헌터", "/Destiny2/3/Profile/{here}/Character/{here}/Vendors/{here}/?components=400,401,402"},
			{"타이탄", "/Destiny2/3/Profile/{here}/Character/{here}/Vendors/{here}/?components=400,401,402"},
			{"워록", "/Destiny2/3/Profile/{here}/Character/{here}/Vendors/{here}/?components=400,401,402"}
	};
	
	private List<JSONObject> list = new ArrayList<>();
	private Map<String, String> map = new HashMap<>();
	private JSONObject manifest;
	
	public Bungie(String access, String refresh, String log) {
		super(access, refresh, log);
		
		for(int i=0; i<3; i++)
			list.add(new JSONObject());
		
		JSONObject target = getManifest();
		if(target != null) {
			manifest = target;
		}
		
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> {
			JSONObject ob = getManifest();
			if(ob != null) {
				manifest = ob;
			}
		}, WeekTime(DayOfWeek.SATURDAY), 7L * 24L * 60L * 60L * 1000L, TimeUnit.MILLISECONDS);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> xur(){
		JSONObject a = getManifest();
		if(a != null) {
			manifest = a;
		}
		JSONObject xur = null;
		Map<String, String> armor = new LinkedHashMap<>();
		Set<Pair<String, String>> headers = new HashSet<>();
		headers.add(new Pair<>("x-api-key", x_api_key));
		headers.add(new Pair<>("Authorization", "Bearer " + super.getAccess_Token()));
		
		/*
         * 방어구 이름 파트
         */
		for(String[] target : Class) {
			String tClass = target[0];
			String tUrl = target[1];
			xur = super.get(headers, base + tUrl);
			armor.put(tClass, getItemDefine(xur));
		}
		
		/*
		 * 무기 파트
		 * 고민중..... 매의 달, 경이 암호, 경이 엔그램 표현을 할지 말지.
		 * 결국 암호빼고 다 함.
		 */
		Map<String, Map<String, Map<String, String>>> item = new LinkedHashMap<>();
		item.put("경이", new LinkedHashMap<String, Map<String, String>>());
		item.put("전설", new LinkedHashMap<String, Map<String, String>>());
		for(int key : xur.getJSONObject("Response").getJSONObject("sales").getJSONObject("data").keySet().stream().mapToInt(Integer::parseInt).sorted().toArray()) {
			String itemhash = String.valueOf(xur.getJSONObject("Response").getJSONObject("sales").getJSONObject("data").getJSONObject(String.valueOf(key)).getLong("itemHash"));
			String tierTypeName = list.get(0).getJSONObject(itemhash).getJSONObject("inventory").getString("tierTypeName");
			String name = list.get(0).getJSONObject(itemhash).getJSONObject("displayProperties").getString("name");
			
			if(tierTypeName.equals("경이")) {
				if(name.equals("외계인학")) {
					continue;
				}
				else {
					item.get("경이").put(itemhash, new LinkedHashMap<String, String>());
					item.get("경이").get(itemhash).put("name", name);
					item.get("경이").get(itemhash).put("itemHash", itemhash);
					item.get("경이").get(itemhash).put("icon", list.get(0).getJSONObject(itemhash).getJSONObject("displayProperties").getString("icon"));
				}
			}
			else {
				int itemCategoryHash = list.get(0).getJSONObject(itemhash).getJSONArray("itemCategoryHashes").getInt(2);
				if(itemCategoryHash == 20) {
					continue;
				}
				else {
					item.get("전설").put(itemhash, new LinkedHashMap<String, String>());
					item.get("전설").get(itemhash).put("name", name);
					item.get("전설").get(itemhash).put("itemHash", itemhash);
					item.get("전설").get(itemhash).put("icon", list.get(0).getJSONObject(itemhash).getJSONObject("displayProperties").getString("icon"));
				}
			}
		}
		
		
		/*
		 * 쥴이 떠나는 시간 파트
		 * 일단 String 객체로 다음 refresh time을 알아봐야함.
		 */
		String nextRefreshTime = xur.getJSONObject("Response").getJSONObject("vendor").getJSONObject("data").getString("nextRefreshDate");
		Instant instant = Instant.parse(nextRefreshTime);
		LocalDateTime kor = LocalDateTime.ofInstant(instant, ZoneId.of("UTC+17"));
		LocalDateTime date = kor.plusDays(-3).withHour(2).withMinute(0).withSecond(0).withNano(0);
		Timestamp disableTime = TimeFormat.RELATIVE.atInstant(date.toInstant(ZoneOffset.of("+9")));
		/*
		 * 쥴이 있는 위치 파트
		 * 로케이션 정의는 직접 보고 내가 해야하므로, 이쪽에서는 인덱스만 넘겨줌.
		 */
		int locationIndex = xur.getJSONObject("Response").getJSONObject("vendor").getJSONObject("data").getInt("vendorLocationIndex");
		
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("xur", new LinkedHashMap<String, Object>());
		((Map<String, Object>) map.get("xur")).put("disableTime", disableTime.toString());
		((Map<String, Object>) map.get("xur")).put("locationIndex", locationIndex);
		map.put("item", item);
		map.put("armor", armor);
		
		return map;
	}
	
	private JSONObject getManifest() {
		Set<Pair<String, String>> headers = new HashSet<>();
		headers.add(new Pair<>("x-api-key", x_api_key));
		
		JSONObject manifest = super.get(headers, base + "/Destiny2/Manifest/");
		
		if(manifest == null || manifest.isEmpty()) {
			System.out.println("더이상 진행할 수 없는 오류로 인해 프로그램을 종료합니다.");
			System.exit(0);
		}
		else{
			String currVer = manifest.getJSONObject("Response").getString("version");
			if(currVer.equals(version))
				return null;
			else{
				version = currVer;
				return manifest
						.getJSONObject("Response")
						.getJSONObject("jsonWorldComponentContentPaths")
						.getJSONObject("ko");
			}
		}
		
		return null;
	}
	
	private String getItemDefine(JSONObject object) {
		Set<Pair<String, String>> headers = new HashSet<>();
		headers.add(new Pair<>("x-api-key", x_api_key));
		String target = manifest.getString("DestinyInventoryItemDefinition");
		String url = map.getOrDefault("DestinyInventoryItemDefinition", null);
		if(url != null) {
			if(!target.equals(url)) {
				map.put("DestinyInventoryItemDefinition", target);
				list.set(0, super.get(headers, "https://www.bungie.net" + target));
			}
		}
		else {
			map.put("DestinyInventoryItemDefinition", target);
			list.set(0, super.get(headers, "https://www.bungie.net" + target));
		}
		
		int hash = -1;
		for(String str : object.getJSONObject("Response").getJSONObject("sales").getJSONObject("data").keySet()) {
			int index = Integer.parseInt(str);
			hash = hash < index && index > 217 && index <= 547 ? index : hash;
		}
		long itemhash = object.getJSONObject("Response").getJSONObject("sales").getJSONObject("data").getJSONObject(String.valueOf(hash)).getLong("itemHash");
		long e = list.get(0).getJSONObject(String.valueOf(itemhash)).getLong("collectibleHash");
		return getCollectibleDefine(String.valueOf(e));
	}
	
	private String getCollectibleDefine(String hash) {
		Set<Pair<String, String>> headers = new HashSet<>();
		headers.add(new Pair<>("x-api-key", x_api_key));
		
		String target = manifest.getString("DestinyCollectibleDefinition");
		String url = map.getOrDefault("DestinyCollectibleDefinition", null);
		if(url != null) {
			if(!target.equals(url)) {
				map.put("DestinyCollectibleDefinition", target);
				list.set(1, super.get(headers, "https://www.bungie.net" + target));
			}
		}
		else {
			map.put("DestinyCollectibleDefinition", target);
			list.set(1, super.get(headers, "https://www.bungie.net" + target));
		}
		
		return getPresentNodeDefine(String.valueOf(list.get(1).getJSONObject(hash).getJSONArray("parentNodeHashes").get(0)));
	}
	
	private String getPresentNodeDefine(String hash) {
		Set<Pair<String, String>> headers = new HashSet<>();
		headers.add(new Pair<>("x-api-key", x_api_key));
		
		String target = manifest.getString("DestinyPresentationNodeDefinition");
		String url = map.getOrDefault("DestinyPresentationNodeDefinition", null);
		if(url != null) {
			if(!target.equals(url)) {
				map.put("DestinyPresentationNodeDefinition", target);
				list.set(2, super.get(headers, "https://www.bungie.net" + target));
			}
		}
		else {
			map.put("DestinyPresentationNodeDefinition", target);
			list.set(2, super.get(headers, "https://www.bungie.net" + target));
		}
		
		return list.get(2).getJSONObject(hash).getJSONObject("displayProperties").getString("name");
	}
	
	private long WeekTime(DayOfWeek day) {
		LocalDateTime target = LocalDateTime
				.now().with(TemporalAdjusters.nextOrSame(day))
				.withHour(0)
				.withMinute(0)
				.withSecond(0)
				.withNano(0);
		LocalDateTime curr = LocalDateTime.now();
		long res = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
				- curr.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		return res < 0 ? res + (7L * 24L * 60L * 60L * 1000) : res;
	}
}
