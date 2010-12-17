package ca.scotthyndman.game.engine.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionUtil {
	
	public static <T> List<T> list(T...elements) {
		List<T> list = new ArrayList<T>();
		for (T ele : elements) {
			list.add(ele);
		}
		return list;
	}
	
	public static <T> Set<T> set(T...elements) {
		Set<T> set = new HashSet<T>();
		for (T ele : elements) {
			set.add(ele);
		}
		return set;
	}
}
