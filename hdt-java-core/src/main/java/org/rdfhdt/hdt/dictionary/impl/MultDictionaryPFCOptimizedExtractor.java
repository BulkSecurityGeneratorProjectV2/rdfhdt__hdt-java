package org.rdfhdt.hdt.dictionary.impl;

import org.rdfhdt.hdt.dictionary.impl.section.PFCDictionarySectionMap;
import org.rdfhdt.hdt.dictionary.impl.section.PFCOptimizedExtractor;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.util.LiteralsUtils;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class MultDictionaryPFCOptimizedExtractor implements OptimizedExtractor{
	private final PFCOptimizedExtractor shared, subjects, predicates;
	private final TreeMap<String,PFCOptimizedExtractor> objects;
	private final long numshared;

	public MultDictionaryPFCOptimizedExtractor(MultipleSectionDictionary origDict) {
		numshared=(int) origDict.getNshared();
		shared = new PFCOptimizedExtractor((PFCDictionarySectionMap) origDict.shared);
		subjects = new PFCOptimizedExtractor((PFCDictionarySectionMap) origDict.subjects);
		predicates = new PFCOptimizedExtractor((PFCDictionarySectionMap) origDict.predicates);
		objects = new TreeMap<>();
		Iterator iterator = origDict.getAllObjects().entrySet().iterator();
		while (iterator.hasNext()){
			Map.Entry entry = (Map.Entry)iterator.next();
			objects.put((String)entry.getKey(),new PFCOptimizedExtractor((PFCDictionarySectionMap)entry.getValue()));
		}
	}

	@Override
	public CharSequence idToString(long id, TripleComponentRole role) {
		AbstractMap.SimpleEntry<String,PFCOptimizedExtractor> section = getSection(id, role);
		long localId = getLocalId(id, role);
		if(section.getKey().equals("NO_DATATYPE") || section.getKey().equals("section"))
			return section.getValue().extract(localId);
		else {
			String label = section.getValue().extract(localId).toString();
			String dType = section.getKey();
			//Matcher matcher = pattern.matcher(label);
			if(LiteralsUtils.containsLanguage(label)){
				return label;
			}else{
				return label + "^^" + dType;
			}
		}
	}
	private AbstractMap.SimpleEntry<String,PFCOptimizedExtractor> getSection(long id, TripleComponentRole role) {
		switch (role) {
		case SUBJECT:
			if(id<=numshared) {
				return new AbstractMap.SimpleEntry<>("section",shared);
			} else {
				return new AbstractMap.SimpleEntry<>("section",subjects);
			}
		case PREDICATE:
			return new AbstractMap.SimpleEntry<>("section",predicates);
		case OBJECT:
			if(id<= numshared) {
				return new AbstractMap.SimpleEntry<>("section",shared);
		} else {
			Iterator hmIterator = objects.entrySet().iterator();
			// iterate over all subsections in the objects section
			PFCOptimizedExtractor desiredSection = null;
			String type = "";
			int count = 0;
			while (hmIterator.hasNext()){
				Map.Entry entry = (Map.Entry)hmIterator.next();
				PFCOptimizedExtractor subSection = (PFCOptimizedExtractor)entry.getValue();
				count+= subSection.getNumStrings();
				if(id <= numshared+count){
					desiredSection = subSection;
					type = (String)entry.getKey();
					break;
				}
			}
			return new AbstractMap.SimpleEntry<>(type,desiredSection);
		}
		}
		throw new IllegalArgumentException();
	}

	
	private long getLocalId(long id, TripleComponentRole position) {
		switch (position) {
		case SUBJECT:
			if(id <= numshared)
				return id;
			else
				return id - numshared;
		case OBJECT:
			if(id<=numshared) {
				return id;
			} else {
				Iterator hmIterator = objects.entrySet().iterator();
				// iterate over all subsections in the objects section
				long count = 0;
				while (hmIterator.hasNext()){
					Map.Entry entry = (Map.Entry)hmIterator.next();
					PFCOptimizedExtractor subSection = (PFCOptimizedExtractor)entry.getValue();
					count+= subSection.getNumStrings();
					if(id <= numshared+ count){
						count -= subSection.getNumStrings();
						break;
					}
				}
				// subtract the number of elements in the shared + the subsections in the objects section
				return id - count - numshared;
			}
		case PREDICATE:
			return id;
		}

		throw new IllegalArgumentException();
	}
}
