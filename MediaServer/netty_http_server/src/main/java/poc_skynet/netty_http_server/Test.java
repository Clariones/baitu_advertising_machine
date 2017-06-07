package poc_skynet.netty_http_server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	public static void main(String[] args) {
		String a= "public/baitu_advertise/resource/media/225/212/48/55/Screenshot_from_2016-07-27_19-58-31.png";
		Pattern ptnFSMediaInfo = Pattern.compile("(\\w+)/(\\w+)/(.*?)/(\\d+/){4}([^/]+)");
		
		Matcher m = ptnFSMediaInfo.matcher(a);
		System.out.println(m.matches());
		System.out.println(m.groupCount());
		for(int i=1;i<=m.groupCount();i++){
			System.out.println(m.group(i));
		}

	}

}
