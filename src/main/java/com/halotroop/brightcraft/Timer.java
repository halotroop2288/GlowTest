package com.halotroop.brightcraft;

import java.util.ArrayList;
import java.util.List;

public class Timer {
	protected static final int TARGET_TPS = 20;
	private static final List<Tickable> tickables = new ArrayList<>();
	protected static long lastTick;
	protected static long nextTick;
	
	public static void registerTickable(Tickable listener) {
		tickables.add(listener);
	}
	
	public static void cleanup() {
		for (Tickable tickable : tickables) {
			tickables.remove(tickable);
		}
	}
	
	public static int getTickRate() {
		return TARGET_TPS;
	}
	
	public static void reset() {
		lastTick = 0;
		nextTick = 0;
	}
	
	public static void tick() {
		long currentTime = System.currentTimeMillis();
		
		if (currentTime >= nextTick) {
			long targetTimeDelta = 1000L / TARGET_TPS;
			
			if (lastTick == 0 || nextTick == 0) {
				lastTick = currentTime - targetTimeDelta;
				nextTick = currentTime;
			}
			
			float deltaTime = (float) (currentTime - lastTick) / targetTimeDelta;
			
			for (Tickable tickable : tickables) {
				tickable.tick();
			}
			
			lastTick = currentTime;
			nextTick = currentTime + targetTimeDelta;
		}
	}
	
	public interface Tickable {
		void tick();
	}
}
