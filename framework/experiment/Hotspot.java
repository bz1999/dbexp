/**
 * 
 */
package dbexp.framework.experiment;

import java.util.Random;

/**
 * This class defines a hotspot for the purposes of performance evaluation a hotspot is
 * a set of row in a database table that is acted upon at a higher rate than the average rate for
 * all other rows
 * @author Shirley Goldrei
 *
 */
public class Hotspot {
	
	public enum Distribution {SCATTERED, CONTIGUOUS};
	
	private static final int HOTSPOT_FREQUENCY = 90; //%
	private static final int COMPLEMENT_HOTSPOT_FREQUENCY = 100 - HOTSPOT_FREQUENCY;
	
	private final int mHotspotSize;
	private final int mKeyspaceSize;
	private final int mScatterFactor;
	private final Distribution mDistribution;
	private final Random mPseudoRandomizer;
	
	
	public Hotspot(Random aRandomNumberGen, int aHotspotSize, int aKeyspaceSize) {
		this( aRandomNumberGen, aHotspotSize, aKeyspaceSize, Distribution.CONTIGUOUS);
	}
	
	public Hotspot(Random aRandomNumberGen, int aHotspotSize, int aKeyspaceSize, Distribution aDistribution) {
		mHotspotSize = aHotspotSize;
		mKeyspaceSize = aKeyspaceSize;
		mScatterFactor = mKeyspaceSize / mHotspotSize;
		mDistribution = aDistribution;
		mPseudoRandomizer = aRandomNumberGen;
	}
	
	public int getNextKeyId() {
		switch (mDistribution) {
		case SCATTERED: return getScatteredNextKeyId();
		case CONTIGUOUS: return getContiguousNextKeyId();
		default:
			return getContiguousNextKeyId();
		}

	}
	
	private int getContiguousNextKeyId() {
		int randomID;
		// outside the hotspot
        if (mPseudoRandomizer.nextInt(100) < COMPLEMENT_HOTSPOT_FREQUENCY) {
            randomID = mPseudoRandomizer.nextInt(mKeyspaceSize - mHotspotSize) + mHotspotSize + 1;
        } else { // inside the hotspot
            randomID = mPseudoRandomizer.nextInt(mHotspotSize) + 1;
        }
        return randomID;
	}
	
	private int getScatteredNextKeyId() {
		int randomID;
		// outside the hotspot
        if (mPseudoRandomizer.nextInt(100) < COMPLEMENT_HOTSPOT_FREQUENCY) {
        	int randomColdItem = mPseudoRandomizer.nextInt(mKeyspaceSize - mHotspotSize);
        	int numberOfHotItemsUpToRandomColdItem = (randomColdItem/(mScatterFactor-1)) +1;
            randomID = randomColdItem + numberOfHotItemsUpToRandomColdItem + 1;
        } else { // inside the hotspot
            randomID = (mPseudoRandomizer.nextInt(mHotspotSize) * mScatterFactor) + 1;
        }
        return randomID;
	}

}
