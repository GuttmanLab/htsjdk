package htsjdk.samtools.fork.util.iterators;

import htsjdk.samtools.fork.FilteredSAMRecordIterator;
import htsjdk.samtools.fork.SAMFileHeader;
import htsjdk.samtools.fork.SAMRecord;
import htsjdk.samtools.fork.SAMRecordIterator;
import htsjdk.samtools.fork.SamReader;
import htsjdk.samtools.fork.SamReaderFactory;
import htsjdk.samtools.util.CloseableIterator;

import java.io.File;
import java.io.IOException;

/**
 * Iterate over a query-sorted bam file and return number of mappings for each query
 * @author prussell
 *
 */
public class MappingCountPerQuery implements CloseableIterator<Integer> {
	
	private SamReader samReader;
	private FilteredSAMRecordIterator iter;
	String currQuery;
	int currCount;
	
	/**
	 * @param bamFile Bam file to iterate over
	 */
	public MappingCountPerQuery(String bamFile) {
		samReader = SamReaderFactory.makeDefault().open(new File(bamFile));
		iter = new FilteredSAMRecordIterator(samReader.iterator());
		iter.addRequiredCondition(samRecord -> !samRecord.getReadUnmappedFlag());
		iter.assertSorted(SAMFileHeader.SortOrder.queryname);
		if(!iter.hasNext()) {
			throw new IllegalArgumentException("SAM record iterator is empty");
		}
		currCount = 1;
		currQuery = iter.next().getReadName();
	}

	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public Integer next() {
		while(iter.hasNext()) {
			String nextQuery = iter.next().getReadName();
			if(nextQuery.equals(currQuery)) {
				currCount++;
				continue;
			}
			else {
				Integer rtrn = Integer.valueOf(currCount);
				currCount = 1;
				currQuery = nextQuery;
				return rtrn;
			}
		}
		return Integer.valueOf(currCount);
	}

	@Override
	public void close() {
		iter.close();
		try {
			samReader.close();
		} catch (IOException e) {
			System.err.println("Couldn't close SAM reader");
			e.printStackTrace();
		}
	}
	
	
	
}
