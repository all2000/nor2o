/*
 * Copyright (C) 2009 Ontology Engineering Group, Departamento de Inteligencia Artificial
 * 					  Facultad de Informática, Universidad Politécnica de Madrid, Spain
 * 					  boricles
 *	
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.upm.fi.dia.oeg.nor2o.or;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.RDFXMLOntologyFormat;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLLabelAnnotation;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyFormat;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.model.OWLPropertyAxiom;
import org.semanticweb.owl.model.UnknownOWLOntologyException;
import org.semanticweb.owl.util.SimpleURIMapper;
import org.semanticweb.owl.vocab.OWLRDFVocabulary;
import org.semanticweb.owl.vocab.XSDVocabulary;

/**
 * @author boricles
 *
 */
public class OROWL extends OR {

	
	protected OWLOntologyManager manager; 
	OWLOntology ontology;
	OWLDataFactory factory;
	
	/**
	 * @throws  
	 * 
	 */
	public OROWL()  {
		super();
	}

	public void init() {
		manager = OWLManager.createOWLOntologyManager();
		File file = new File(pURI);
		
		physicalURI = file.toURI();
		
		ontologyURI = URI.create(oURI);
		//physicalURI = URI.create(pURI);

		SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);

		manager.addURIMapper(mapper);

		try {
			if (alreadyExists.equals(OR.NO))
				ontology = manager.createOntology(ontologyURI);
			else
				ontology = manager.loadOntology(ontologyURI);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		factory = manager.getOWLDataFactory();
	}
	
	public void createClass(String className) {
		try {
			String clazzName = encode(className);
			
			OWLClass cls = factory.getOWLClass(URI.create(ontologyURI + separator + clazzName));
			AddAxiom ae = new AddAxiom( ontology, factory.getOWLDeclarationAxiom(cls) );

			manager.applyChange(ae);
			
		} catch (OWLOntologyChangeException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	
	
	public Set<URI> getTopLevelClasses() {
		//super.getTopLevelClasses();
		Set<URI> topLevelClasses = new HashSet<URI>();
		
		Set<OWLClass> allClasses = ontology.getReferencedClasses();
		
		for (OWLClass clazz : allClasses) {
			Set<OWLDescription> superClasses = clazz.getSuperClasses(ontology);
			if (superClasses == null || superClasses.isEmpty()) {
				topLevelClasses.add(clazz.getURI());
			}
		}
		
		return topLevelClasses;
	}
	
	public void createObjectPropertyAxiom(Set<URI>domainClass, String rangeClazz, int axiom, String adhocz) {
		try {
			OWLClass clsRange;
			Set<OWLClass> domainClasses = new HashSet<OWLClass>();
			String rangeClass = encode(rangeClazz);
			
			clsRange = factory.getOWLClass(URI.create(ontologyURI + separator + rangeClass));
			
			for (URI dClass : domainClass) {
				domainClasses.add(factory.getOWLClass(dClass));
			}

			Set<OWLAxiom> owlAxioms = new HashSet<OWLAxiom>();
			
			if (axiom==EQUIVALENT_CLASS_AXIOM) {
				for (OWLClass cClazz : domainClasses) {
					owlAxioms.add(factory.getOWLEquivalentClassesAxiom(cClazz, clsRange));
				}
			}
			
			if (axiom==SUBCLASSOF_AXIOM) {
				for (OWLClass cClazz : domainClasses) {
					owlAxioms.add(factory.getOWLSubClassAxiom(cClazz, clsRange));
				}
			}
			
			if (axiom==ADHOC_RELATION_CLASS_AXIOM) {
				String adhoc = encode(adhocz);
				
				OWLObjectProperty adhocRel = factory.getOWLObjectProperty(URI.create(ontologyURI + separator+ adhoc));
				for (OWLClass cClazz : domainClasses) {
					owlAxioms.add(factory.getOWLObjectPropertyDomainAxiom(adhocRel, cClazz));
					owlAxioms.add(factory.getOWLObjectPropertyRangeAxiom(adhocRel, clsRange));
					manager.addAxioms(ontology, owlAxioms);		
					owlAxioms.clear();
					owlAxioms = null;
				}
			}
			
			if (owlAxioms != null) {
				manager.addAxioms(ontology, owlAxioms);
				owlAxioms.clear();
				owlAxioms = null;
			}

		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		} 
	}

	
	public void createObjectPropertyAxiom(String clazz1, String clazz2, int axiom, String adhocz) {
		try {
			OWLClass clsA,clsB;
			String class1 =	encode(clazz1);
			String class2 = encode(clazz2);
			String adhoc = encode(adhocz);
			
			if (!class1.contains(CLASS[getImplementationLanguage()]))
				clsA = factory.getOWLClass(URI.create(ontologyURI + separator + class1));
			else 
				clsA = factory.getOWLClass(URI.create(class1));
			if (!class2.contains(CLASS[getImplementationLanguage()]))
				clsB = factory.getOWLClass(URI.create(ontologyURI + separator + class2));
			else
				clsB = factory.getOWLClass(URI.create(class2));			
			
			OWLAxiom owlAxiom = null;
			
			if (axiom==EQUIVALENT_CLASS_AXIOM) {
				owlAxiom = factory.getOWLEquivalentClassesAxiom(clsA, clsB);
			}
			
			if (axiom==SUBCLASSOF_AXIOM) {
				owlAxiom = factory.getOWLSubClassAxiom(clsA, clsB);		
			}
			
			if (axiom==ADHOC_RELATION_CLASS_AXIOM) {
				
				OWLObjectProperty adhocRel = factory.getOWLObjectProperty(URI.create(ontologyURI + separator+ adhoc));
				
				Set<OWLAxiom> domainsAndRanges = new HashSet<OWLAxiom>();
				domainsAndRanges.add(factory.getOWLObjectPropertyDomainAxiom(adhocRel, clsA));
				domainsAndRanges.add(factory.getOWLObjectPropertyRangeAxiom(adhocRel, clsB));
	
				manager.addAxioms(ontology, domainsAndRanges);
			}
			
			if (owlAxiom != null) {
				AddAxiom addAxiom = new AddAxiom(ontology, owlAxiom);
				manager.applyChange(addAxiom);
			}

		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		} 
		 
	}
	
	public void createDataTypePropertyAxiom(String clazz1, String dtpNamez, String type) {
		try {
			String class1 = encode(clazz1);
			String dtpName = encode(dtpNamez);
			
			OWLClass clsA = factory.getOWLClass(URI.create(ontologyURI + separator + class1));
			OWLDataProperty dtProp = factory.getOWLDataProperty(URI.create(ontologyURI + separator + dtpName));
			OWLDataType dataType = factory.getOWLDataType(new URI(type));
			Set<OWLAxiom> domainsAndRanges = new HashSet<OWLAxiom>();
			domainsAndRanges.add(factory.getOWLDataPropertyDomainAxiom(dtProp, clsA));
			domainsAndRanges.add(factory.getOWLDataPropertyRangeAxiom(dtProp, dataType));
			
			manager.addAxioms(ontology, domainsAndRanges);
			
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void createIndividualAxiom(String clazz1, String individualz) {
		try {
			//System.out.println("class " + clazz1);
			//System.out.println("individual " + individualz);			
			String class1 = encode(clazz1);
			String individual = encode(individualz);
			OWLClass clsA = factory.getOWLClass(URI.create(ontologyURI + separator + class1));
			OWLIndividual owlIndividual = factory.getOWLIndividual(URI.create(ontologyURI + separator + individual));
			OWLClassAssertionAxiom classAssertionAx = factory.getOWLClassAssertionAxiom(owlIndividual, clsA);
			manager.addAxiom(ontology, classAssertionAx);
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		} 
	}
	
	public void createIndividualLabelAxiom(String individualz, String label) {
		try {
			String individual = encode(individualz);
			OWLLabelAnnotation owlLabel = factory.getOWLLabelAnnotation(label);
			OWLIndividual owlInd = factory.getOWLIndividual(URI.create(ontologyURI + separator + individual));
			OWLAxiom owlLabelAxiom = factory.getOWLEntityAnnotationAxiom(owlInd, owlLabel);
			manager.addAxiom(ontology, owlLabelAxiom);
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		} 		
	}

	public void createObjectPropertyLabelAxiom(String adhocz, String label) {
		try {
			String adhoc = encode(adhocz);
			OWLLabelAnnotation owlLabel = factory.getOWLLabelAnnotation(label);
			OWLObjectProperty adhocRel = factory.getOWLObjectProperty(URI.create(ontologyURI + separator+ adhoc));
			OWLAxiom owlLabelAxiom = factory.getOWLEntityAnnotationAxiom(adhocRel, owlLabel);
			manager.addAxiom(ontology, owlLabelAxiom);
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		} 	
	}

	public void createDataPropertyLabelAxiom(String dtpNamez, String label) {
		try {
			String dtpName = encode(dtpNamez);
			OWLLabelAnnotation owlLabel = factory.getOWLLabelAnnotation(label);
			OWLDataProperty dtProp = factory.getOWLDataProperty(URI.create(ontologyURI + separator + dtpName));
			OWLAxiom owlLabelAxiom = factory.getOWLEntityAnnotationAxiom(dtProp, owlLabel);
			manager.addAxiom(ontology, owlLabelAxiom);
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		} 	
	}
	
	
	public void createObjectPropertyIndividualAxiom (String ind1z, String ind2z, int axiom, String adhocz) {
		try {		
			String ind1 = encode(ind1z);
			String ind2 = encode(ind2z);
			String adhoc = encode(adhocz);
			
			if (axiom==SUBCLASSOF_AXIOM || axiom==EQUIVALENT_CLASS_AXIOM) 
				return;
			
			OWLIndividual owlInd1 = factory.getOWLIndividual(URI.create(ontologyURI + separator + ind1));
			OWLIndividual owlInd2 = factory.getOWLIndividual(URI.create(ontologyURI + separator + ind2));
			OWLObjectProperty owlAdhoc = factory.getOWLObjectProperty(URI.create(ontologyURI + separator + adhoc));
			
			OWLAxiom owlObj = factory.getOWLObjectPropertyAssertionAxiom(owlInd1, owlAdhoc, owlInd2);
			manager.addAxiom(ontology, owlObj);
			
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		} 		
	}
	
	public void createDataTypePropertyValueAxiom(String individualz, String dtpNamez, String value,String type) {
		try {
			
			String individual = encode(individualz);
			String dtpName = encode(dtpNamez);
			
			
			OWLIndividual owlInd1 = factory.getOWLIndividual(URI.create(ontologyURI + separator + individual));
			OWLDataProperty dtProp = factory.getOWLDataProperty(URI.create(ontologyURI + separator + dtpName));
			OWLDataType dataType = factory.getOWLDataType(new URI(type));
			
			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

			
			if (type.contains(FLOAT))
				axioms.add(factory.getOWLDataPropertyAssertionAxiom(owlInd1, dtProp, Float.parseFloat(value)));
			if (type.contains(DOUBLE))
				axioms.add(factory.getOWLDataPropertyAssertionAxiom(owlInd1, dtProp, Double.parseDouble(value)));
			//if (type.contains(DECIMAL))
				//axioms.add(factory.getOWLDataPropertyAssertionAxiom(owlInd1, dtProp, Double.parseDouble(value)));
			if (type.contains(STRING))
				axioms.add(factory.getOWLDataPropertyAssertionAxiom(owlInd1, dtProp, value));
			
			axioms.add(factory.getOWLDataPropertyRangeAxiom(dtProp, dataType));			
			
			manager.addAxioms(ontology, axioms);
			
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
	}


	public URI getURIOfClass (String clazzName) {
		String className;
		className = encode(clazzName);
		Set<OWLClass> allClasses = ontology.getReferencedClasses();
		URI uriOfClass = URI.create(ontologyURI + separator + className);
		for (OWLClass clazz : allClasses) {
			if (clazz.getURI().equals(uriOfClass))
				return uriOfClass;
		}
		return null;		
	}

	public void createClassLabelAxiom(String clazz1, String label) {
		try {
			String class1 = encode(clazz1);
			
			OWLLabelAnnotation owlLabel = factory.getOWLLabelAnnotation(label);
			OWLClass clsA = factory.getOWLClass(URI.create(ontologyURI + separator + class1));
			OWLAxiom owlLabelAxiom = factory.getOWLEntityAnnotationAxiom(clsA, owlLabel);
			manager.addAxiom(ontology, owlLabelAxiom);
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		} 	
	}
	
	public void createOntologyAnnotation(String annotation) {
		try {
			OWLAnnotation anno = factory.getCommentAnnotation(annotation);
			OWLAxiom owlAxiom = factory.getOWLOntologyAnnotationAxiom(ontology, anno);
			manager.addAxiom(ontology, owlAxiom);
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}		
	}
	
	
	public void save(){
		try {
			OWLOntologyFormat ofm = manager.getOntologyFormat(ontology);
			
			ofm.setParameter("encoding", DEFAULT_ENCODING);
			//manager.saveOntology(ontology, new RDFXMLOntologyFormat(), physicalURI);    
			manager.setOntologyFormat(ontology, ofm);
			manager.saveOntology(ontology);
			
			
		} catch (UnknownOWLOntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Set getAxioms() {
		return null;
	}	
	
	public Set<OWLEntity> getEntities() {
		return ontology.getReferencedEntities();
	}
	
	public Set<OWLClass> getClasses() {
		return ontology.getReferencedClasses();
	}
	
	public Set<OWLObjectProperty> getObjectProperties() {
		return ontology.getReferencedObjectProperties();
	}
	
	public Set<OWLDataProperty> getDataProperties() {
		return ontology.getReferencedDataProperties();
	}

	public Set<OWLIndividual> getIndividuals() {
		return ontology.getReferencedIndividuals();
	}
	
	protected static String getLabel(String localName) {
		String label = "";
		for (int i=0;i<localName.length();i++) {
			if (Character.isUpperCase(localName.charAt(i)) && i!=0) {
				label += " ";
				label += Character.toLowerCase(localName.charAt(i));
			}
			else
				label += localName.charAt(i);
				
		}
		return label;
	}
	
	public static void main(String []args) throws UnsupportedEncodingException {
		
		//String hola = encode("La mancha");
		//System.out.println(hola);
		/*ORReader orReader = new ORReader();
		orReader.read();
		OR myOR = orReader.getOR();

		String localName,label;
		
		//Map<String> labels = new HashSet<String>();
		Map<String,String> labelsForClass = new HashMap<String,String>();
		Map<String,String> labelsForIndividuals = new HashMap<String,String>();
		Map<String,String> labelsForObjectProperties = new HashMap<String,String>();
		Map<String,String> labelsForDataProperties = new HashMap<String,String>();		
		
		Set<OWLClass> classes = myOR.getClasses();		
		for (OWLClass owlClass : classes) {
			localName = owlClass.getURI().toString();
			localName = localName.substring(localName.indexOf(myOR.getSeparator())+1);
			label = getLabel(localName);
			labelsForClass.put(localName, label);
		}

		Set<OWLIndividual> individuals = myOR.getIndividuals();		
		for (OWLIndividual individual : individuals) {
			localName = individual.getURI().toString();
			localName = localName.substring(localName.indexOf(myOR.getSeparator())+1);
			label = getLabel(localName);
			labelsForIndividuals.put(localName, label);
		}
		
		Set<OWLObjectProperty> oProperties = myOR.getObjectProperties();		
		for (OWLObjectProperty oProperty : oProperties) {
			localName = oProperty.getURI().toString();
			localName = localName.substring(localName.indexOf(myOR.getSeparator())+1);
			label = getLabel(localName);
			labelsForObjectProperties.put(localName, label);
		}

		Set<OWLDataProperty> dProperties = myOR.getDataProperties();		
		for (OWLDataProperty dProperty : dProperties) {
			localName = dProperty.getURI().toString();
			localName = localName.substring(localName.indexOf(myOR.getSeparator())+1);
			label = getLabel(localName);
			labelsForDataProperties.put(localName, label);
		}
		
		
		for (Iterator it = labelsForClass.keySet().iterator(); it.hasNext();) {
			localName = (String)it.next();
			label = labelsForClass.get(localName);
			myOR.createClassLabelAxiom(localName, label);
		}
		
		for (Iterator it = labelsForIndividuals.keySet().iterator(); it.hasNext();) {
			localName = (String)it.next();
			label = labelsForIndividuals.get(localName);
			myOR.createIndividualLabelAxiom(localName, label);
		}
		
		for (Iterator it = labelsForObjectProperties.keySet().iterator(); it.hasNext();) {
			localName = (String)it.next();
			label = labelsForObjectProperties.get(localName);
			myOR.createObjectPropertyLabelAxiom(localName, label);
		}

		for (Iterator it = labelsForDataProperties.keySet().iterator(); it.hasNext();) {
			localName = (String)it.next();
			label = labelsForDataProperties.get(localName);
			myOR.createDataPropertyLabelAxiom(localName, label);
		}
		
		myOR.save();*/
	}

	public void createIndividualLabelAxiom(String individualz, String label, String lang) {
		try {
			String individual = encode(individualz);			
			OWLLabelAnnotation owlLabel = factory.getOWLLabelAnnotation(label,lang);
			OWLIndividual owlInd = factory.getOWLIndividual(URI.create(ontologyURI + separator + individual));
			OWLAxiom owlLabelAxiom = factory.getOWLEntityAnnotationAxiom(owlInd, owlLabel);
			manager.addAxiom(ontology, owlLabelAxiom);
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		} 	
	}
	
	public String encode(String localName, boolean encode) {
		if (encode)
			return encode(localName);
		return localName;
	}
	
	
	public String encode(String localName) {
		String encodedString;
		int pos = localName.lastIndexOf(LAST_SEPARATOR);  //TODO check it 
		String lastPart = localName;
		String firstPart = "";
		if (pos != -1) {
			lastPart = localName.substring(pos+1);
			/*if (lastPart.charAt(0)=='_')		//TODO check it
				lastPart = lastPart.substring(1);*/
			firstPart = localName.substring(0,pos+1);
		}
		try {
			lastPart = URLEncoder.encode(lastPart,DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		encodedString = firstPart + lastPart;
		return encodedString;
		
	}
	
	

	
}
