import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description:
 *
 * @author <a href="ralph.hodgson@pressassociation.com">Ralph Hodgson</a>
 * @since 2017-05-22T14:40
 */
public class ChannelFormatter {
	private static final Logger LOG = LoggerFactory.getLogger(ChannelFormatter.class);
	private static final String MAJOR = "major";
	private static final String END_STY = "end_sty";
	private static final String CHAN_STY = "chan_sty";

	private static Map<String, String> baseMap = new HashMap<>();

	private static Map<String, String> hdMap = new HashMap<>();

	private static Map<String, String> offsetMap = new HashMap<>();

	private static Map<String, String> bothMap = new HashMap<>();

	public static void main(String[] args) throws IOException {

		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("channels.txt");

		IOUtils.readLines(is, "utf-8")
			.forEach(line -> {
				Matcher matcher = Pattern.compile("^\\s+(.*?)\\s+major\\s+\\w+\\s+\\w+\\s+tv\\s*$").matcher(line);

				Style type = Style.BASE;
				String style = CHAN_STY;

				if (matcher.matches()) {
					String nf = matcher.group(1);
					StringBuilder sb = new StringBuilder();

					if (nf.matches("[a-z0-9]+plus[a-z0-9.]+")) {
						type = Style.OFFSET;
						style += "_offset";
					}
					if (nf.matches("[a-z0-9]+hd\\.\\w+")) {
						type = Style.OFFSET.equals(type) ? Style.BOTH : Style.HD;
						style += "_hd";
					}
					sb.append(pad("", 8))
						.append(pad(nf, 36))
						.append(pad("", 8))
						.append(pad(MAJOR, 12))
						.append(pad("", 8))
						.append(pad(style, 20))
						.append(pad("", 8))
						.append(pad(END_STY, 12))
						.append(pad("", 8))
						.append("tv")
						.append("\n");

					switch (type) {
						case BASE:
							baseMap.put(nf, sb.toString());
							break;
						case OFFSET:
							offsetMap.put(nf, sb.toString());
							break;
						case HD:
							hdMap.put(nf, sb.toString());
							break;
						case BOTH:
							bothMap.put(nf, sb.toString());
							break;
					}
				}
			});

		writeMaps();
	}

	private static void writeMaps() {
		StringBuilder out = new StringBuilder();

		out.append("\n");
		out.append(pad("", 8));
		out.append("# Base channels");
		out.append("\n");
		baseMap.keySet().stream().sorted().forEach(nf -> out.append(baseMap.get(nf)));

		out.append("\n");
		out.append(pad("", 8));
		out.append("# Offset channels");
		out.append("\n");
		offsetMap.keySet().stream().sorted().forEach(nf -> out.append(offsetMap.get(nf)));

		out.append("\n");
		out.append(pad("", 8));
		out.append("# HD channels");
		out.append("\n");
		hdMap.keySet().stream().sorted().forEach(nf -> out.append(hdMap.get(nf)));

		out.append("\n");
		out.append(pad("", 8));
		out.append("# Offset and HD channels");
		out.append("\n");
		bothMap.keySet().stream().sorted().forEach(nf -> out.append(bothMap.get(nf)));

		System.out.println(out.toString());

		File file = new File("c:/tmp/channels.txt");

		try {
			FileUtils.writeStringToFile(file, out.toString(), "utf-8");
		} catch (IOException e) {
			LOG.error("Error writing to file: {}", file, e);
		}
	}

	private static String pad(String word, int length) {
		StringBuilder sb = new StringBuilder();
		sb.append(word);

		while (sb.length() < length) {
			sb.append(" ");
		}
		return sb.toString();
	}
}
