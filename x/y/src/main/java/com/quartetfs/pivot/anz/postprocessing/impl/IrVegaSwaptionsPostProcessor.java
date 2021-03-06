package com.quartetfs.pivot.anz.postprocessing.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.ILocationPattern;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.APostProcessor;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesRetriever;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.pivot.anz.utils.ANZUtils;

@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = IrVegaSwaptionsPostProcessor.PLUGIN_KEY)
public class IrVegaSwaptionsPostProcessor extends APostProcessor<Object> implements IPrefetcher {

	private static final long serialVersionUID = 8221205760542025333L;	
	public final static String PLUGIN_KEY = "IR_VEGA_SWAPTIONS";

	private IDimension dimTermBucket;
	private IDimension dimScenTermUnderlying;
	
	private int termBucketOrdinal;
	private int scnTermUnderLyingOrdinal;
	private AnalysisDimensionHelper analysisDimensionHelper=new AnalysisDimensionHelper();
	
	public IrVegaSwaptionsPostProcessor(final String name, final IActivePivot pivot) {
		super(name, pivot);
	}



	@Override
	public void init(final Properties properties) throws QuartetException {
		super.init(properties);
		analysisDimensionHelper.init(properties, pivot);

		String termBucket = properties.getProperty("dimTermBucket"); 
		String scenTermUnderlying = properties.getProperty("dimScenTermUnderlying"); 
		
		dimTermBucket = ANZUtils.findDimensionByName(pivot, termBucket);
		dimScenTermUnderlying = ANZUtils.findDimensionByName(pivot, scenTermUnderlying);
		
		termBucketOrdinal        = dimTermBucket.getOrdinal() - 1;
		scnTermUnderLyingOrdinal = dimScenTermUnderlying.getOrdinal() - 1;
		getPrefetchers().addAll( Collections.<IPrefetcher> singletonList(this) );
	}

	@Override
	public void evaluate(final ILocation locationReceived, final IAggregatesRetriever retriever) throws QuartetException {

		final ILocationPattern locPattern = locationReceived.createWildCardPattern();	
		ILocation newLocation=analysisDimensionHelper.overrideOtherDiscriminatorLocation(locationReceived,null);
		final ICellSet cellSet = retriever.retrieveAggregates(Collections.singletonList(newLocation), Collections.singletonList(underlyingMeasures[0]));
        
		final List<String> termBuckets =  ANZUtils.getSortedTermBucket(dimTermBucket);
		final List<String> underlyingTermBuckets =  ANZUtils.getSortedTermBucket(dimScenTermUnderlying);
		
		ILocation termBucketUnderlyerLoc = createWildCardLocation(newLocation);
		final ICellSet termBucketUnderlyerCellSet =termBucketUnderlyerLoc==null? cellSet: 
				retriever.retrieveAggregates(Collections.singletonList(termBucketUnderlyerLoc), Collections.singletonList(underlyingMeasures[0]));
		
			
		cellSet.forEachLocation( new ILocationProcedure() {
			@Override
			public boolean execute(final ILocation location, final int rowId) {
					
					Object[] values = locPattern.extractValues(location);
					ILocation writeLocation = locPattern.generate(values);
					
					final double currentTermValue = (Double) cellSet.getCellValue(rowId, underlyingMeasures[0]);
					final String termBucket = hasTermValue(location) ? (String)location.getCoordinate(termBucketOrdinal, 1):null;
					final String underlyingTerm = hasTermUnderlyingValue(location) ? (String)location.getCoordinate(scnTermUnderLyingOrdinal, 1) : null;
					
					if(termBucket!=null && underlyingTerm!=null)
					{			
						retriever.write(writeLocation, compute(underlyingTerm,termBucket,location ,currentTermValue)); 	
					}
					else
					{
						retriever.write(writeLocation,  currentTermValue);
					}					
				return true; 
			}
			
			private double compute(String underlyingTerm, String termBucket, ILocation location,double currentTermValue) 
			{			
				final Object[][] termsLocations = location.arrayCopy();
				
				int termBucketIdx = termBuckets.indexOf(termBucket);
				int underlyingTermIdx = underlyingTermBuckets.indexOf(underlyingTerm);
				
				int termBucketSize=termBuckets.size();
				int underlyingTermSize=underlyingTermBuckets.size();
				
				//if both member is not the longest term
				if( (termBucketIdx+1 < termBucketSize) && (underlyingTermIdx+1 < underlyingTermSize))
				{
					//Calc by shifting termBucket					
					Iterator<String> itr = termBuckets.listIterator(termBucketIdx+1);
					String nextTermBucket=null;
					while(itr.hasNext())
					{
						nextTermBucket =itr.next();
						String[] termBucketDimension = new String[2];
						termsLocations[termBucketOrdinal] = termBucketDimension;
						termsLocations[termBucketOrdinal][0] = ILevel.ALLMEMBER;
						termsLocations[termBucketOrdinal][1] = nextTermBucket;
						
						Double nextValue=(Double)termBucketUnderlyerCellSet.getCellValue( new Location(termsLocations) , underlyingMeasures[0]);
						
						if(nextValue==null) continue;// try for next termbucket
						return currentTermValue-nextValue;
					}					
					
					return calcUsingNextUnderlyingTermBucket(currentTermValue,termsLocations,underlyingTermIdx);					
				}
				//If term bucket is longest & underlyingterm bucket is not longest 
				else if((termBucketIdx+1 == termBucketSize) && (underlyingTermIdx+1 < underlyingTermSize))
				{
					//If calc by termbucket does't work then calc by shifting underlyingTermBucket
					return calcUsingNextUnderlyingTermBucket( currentTermValue,termsLocations,underlyingTermIdx);		
				}				
				return currentTermValue;
			}

			private double calcUsingNextUnderlyingTermBucket(double currentTermValue,final Object[][] termsLocations, int underlyingTermSize) 
			{
				Iterator<String> itr;
				//If calc by termbucket does't work then calc by shifting underlyingTermBucket
				itr = underlyingTermBuckets.listIterator(underlyingTermSize+1);
				while(itr.hasNext())
				{
					String nextUnderlyingBucket = itr.next();
					String[] underlyingDimension = new String[2];
					termsLocations[scnTermUnderLyingOrdinal] = underlyingDimension;
					termsLocations[scnTermUnderLyingOrdinal][0] = ILevel.ALLMEMBER;
					termsLocations[scnTermUnderLyingOrdinal][1] = nextUnderlyingBucket;	
					for(String currentTerm : termBuckets)
					{
						String[] termBucketDimension = new String[2];
						termsLocations[termBucketOrdinal] = termBucketDimension;
						termsLocations[termBucketOrdinal][0] = ILevel.ALLMEMBER;
						termsLocations[termBucketOrdinal][1] = currentTerm;	
						
						Double nextValue=(Double)termBucketUnderlyerCellSet.getCellValue( new Location(termsLocations) , underlyingMeasures[0]);
						if(nextValue==null) continue;// try for next termbucket						
						return currentTermValue-nextValue;
					}
				}
				return currentTermValue;
			}		
		}); 


	}

	@Override
	public String getType() {
		return PLUGIN_KEY;
	}

	@Override
	public Collection<ILocation> computeLocations(final Collection<ILocation> locations) {
		final Set<ILocation> locs = new HashSet<ILocation>(); 
		for (final ILocation location : locations) {
			
			ILocation wildCardLoc = createWildCardLocation(location);
			if(!wildCardLoc.equals(location))
			{
				locs.add(analysisDimensionHelper.overrideOtherDiscriminatorLocation(wildCardLoc,null));
			}
			locs.add(location);

		}
		return locs;
	}



	private ILocation createWildCardLocation(ILocation location) 
	{
		Object[][] locationCopy = location.arrayCopy();
		if(hasTermValue(location))
		{
			locationCopy[termBucketOrdinal][1]=null;
		}
		if(hasTermUnderlyingValue(location))
		{
			locationCopy[scnTermUnderLyingOrdinal][1]=null;
		}	
		return new Location(locationCopy);		
		
	}

	@Override
	public Collection<String> computeMeasures(final Collection<ILocation> locations) {
		return Arrays.asList(underlyingMeasures[0]);
	}
	
	private boolean hasTermValue(ILocation location) 
	{
		return location.getLevelDepth(termBucketOrdinal) >=2;
	}
	
	private boolean hasTermUnderlyingValue(ILocation location) 
	{
		return location.getLevelDepth(scnTermUnderLyingOrdinal) >=2;
	}


	
}