package com.yiitz.battery;

class LevelStepTracker {
    private static final int MAX_LEVEL_STEPS = 200;

    public long mLastStepTime;
    public int mNumStepDurations;
    public final long[] mStepDurations;

    public LevelStepTracker() {
        mStepDurations = new long[MAX_LEVEL_STEPS];
        init();
    }
    
    public void init() {
        mLastStepTime = -1;
        mNumStepDurations = 0;
	}
    
    public long computeTimePerLevel() {
        final long[] steps = mStepDurations;
        final int numSteps = mNumStepDurations;

        // For now we'll do a simple average across all steps.
        if (numSteps <= 0) {
            return -1;
        }
        long total = 0;
        for (int i=0; i<numSteps; i++) {
            total += steps[i];
        }
        return total / numSteps;
    }
    
    public void addLevelSteps(int numStepLevels,long elapsedRealtime) {
        int stepCount = mNumStepDurations;
        final long lastStepTime = mLastStepTime;
        if (lastStepTime >= 0 && numStepLevels > 0) {
            final long[] steps = mStepDurations;
            long duration = elapsedRealtime - lastStepTime;
            for (int i=0; i<numStepLevels; i++) {
                System.arraycopy(steps, 0, steps, 1, steps.length-1);
                long thisDuration = duration / (numStepLevels-i);
                duration -= thisDuration;
                steps[0] = thisDuration;
            }
            stepCount += numStepLevels;
            if (stepCount > steps.length) {
                stepCount = steps.length;
            }
        }
        mNumStepDurations = stepCount;
        mLastStepTime = elapsedRealtime;
    }
}
