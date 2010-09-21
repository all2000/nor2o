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

package es.upm.fi.dia.oeg.nor2o;

import java.util.Set;

import org.apache.axis.types.Entity;

import es.upm.fi.dia.oeg.nor2o.nor.NOR;
import es.upm.fi.dia.oeg.nor2o.nor.content.CEntity;
import es.upm.fi.dia.oeg.nor2o.nor.schema.Schema;
import es.upm.fi.dia.oeg.nor2o.nor.util.NORReader;
import es.upm.fi.dia.oeg.nor2o.or.OR;
import es.upm.fi.dia.oeg.nor2o.or.ORReader;
import es.upm.fi.dia.oeg.nor2o.transformation.PRNOR;
import es.upm.fi.dia.oeg.nor2o.transformation.PRNORReader;

/**
 * @author boricles
 *
 */
public class PRNORConverter {

	/**
	 * 
	 */
	public PRNORConverter() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NORReader norReader = new NORReader();
		norReader.read();
		NOR myNOR = norReader.getNOR();
		
		/*Set<CEntity> entities = myNOR.getContent().getEntities();
		Schema schema = myNOR.getSchema();
		System.out.println("========");
		for (CEntity ent : entities) {
			System.out.println(ent.getAttributeValuesAsString("CSName")[0]);
		}*/
		
		ORReader orReader = new ORReader();
		orReader.read();
		OR myOR = orReader.getOR();
		
		PRNORReader prnorReader = new PRNORReader();
		prnorReader.read();
		
		PRNOR myPRNOR = prnorReader.getPRNOR();
		
		myPRNOR.setNor(myNOR);
		myPRNOR.setOr(myOR); 
		
		myPRNOR.transform();


	}

}
