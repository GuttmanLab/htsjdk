package htsjdk.samtools.fork;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import htsjdk.samtools.fork.SAMFileHeader.SortOrder;
import htsjdk.samtools.fork.SamReader.AssertingIterator;
import htsjdk.samtools.util.CloseableIterator;

/**
 * A SAMRecordIterator that only returns SAMRecords satisfying a collection of predicates
 * @author prussell
 *
 */
public class FilteredSAMRecordIterator implements SAMRecordIterator {
	
	private AssertingIterator assertingIter;
	private SAMRecord next;
	private List<Predicate<SAMRecord>> requiredConditions;
	
	/**
	 * @param iter SAMRecord iterator to wrap
	 */
	public FilteredSAMRecordIterator(CloseableIterator<SAMRecord> iter) {
		assertingIter = new AssertingIterator(iter);
		if(assertingIter.hasNext()) {
			next = assertingIter.next();
		}
		requiredConditions = new ArrayList<Predicate<SAMRecord>>();
	}
	
	@Override
	public void close() {
		assertingIter.close();
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public SAMRecord next() {
		SAMRecord rtrn = next;
		while(assertingIter.hasNext()) {
			SAMRecord assertingIterNext = assertingIter.next();
			boolean passed = true;
			for(Predicate<SAMRecord> predicate : requiredConditions) {
				if(!predicate.test(assertingIterNext)) {
					passed = false;
					break;
				}
			}
			if(!passed) continue;
			next = assertingIterNext;
			return rtrn;
		}
		next = null;
		return rtrn;
	}

	@Override
	public SAMRecordIterator assertSorted(SortOrder sortOrder) {
		assertingIter.assertSorted(sortOrder);
		return this;
	}
	
	/**
	 * Add a condition that must be satisfied by SAMRecords in order to be returned by this iterator
	 * If the predicate evaluates to false, the record will be skipped
	 * @param predicate Predicate
	 */
	public void addRequiredCondition(Predicate<SAMRecord> predicate) {
		requiredConditions.add(predicate);
		System.err.println("Added predicate " + predicate.getClass().getSimpleName());
	}
	

}
