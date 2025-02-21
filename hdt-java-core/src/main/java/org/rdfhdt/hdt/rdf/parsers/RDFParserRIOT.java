/**
 * File: $HeadURL: https://hdt-java.googlecode.com/svn/trunk/hdt-java/src/org/rdfhdt/hdt/rdf/parsers/RDFParserRIOT.java $
 * Revision: $Rev: 191 $
 * Last modified: $Date: 2013-03-03 11:41:43 +0000 (dom, 03 mar 2013) $
 * Last modified by: $Author: mario.arias $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Contacting the authors:
 *   Mario Arias:               mario.arias@deri.org
 *   Javier D. Fernandez:       jfergar@infor.uva.es
 *   Miguel A. Martinez-Prieto: migumar2@infor.uva.es
 */

package org.rdfhdt.hdt.rdf.parsers;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.rdf.RDFParserCallback;
import org.rdfhdt.hdt.triples.TripleString;
import org.rdfhdt.hdt.util.io.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mario.arias
 *
 */
public class RDFParserRIOT implements RDFParserCallback, StreamRDF {
	private static final Logger log = LoggerFactory.getLogger(RDFParserRIOT.class);
	private void parse(InputStream stream, String baseUri, Lang lang, boolean keepBNode) {
		if (keepBNode) {
			RDFParser.source(stream).base(baseUri).lang(lang).labelToNode(LabelToNode.createUseLabelAsGiven()).parse(this);
		} else {
			RDFParser.source(stream).base(baseUri).lang(lang).parse(this);
		}
	}

	private RDFCallback callback;
	private TripleString triple = new TripleString();

	/* (non-Javadoc)
	 * @see hdt.rdf.RDFParserCallback#doParse(java.lang.String, java.lang.String, hdt.enums.RDFNotation, hdt.rdf.RDFParserCallback.Callback)
	 */
	@Override
	public void doParse(String fileName, String baseUri, RDFNotation notation, boolean keepBNode, RDFCallback callback) throws ParserException {
		try (InputStream input = IOUtil.getFileInputStream(fileName)) {
			doParse(input, baseUri, notation, keepBNode, callback);
		} catch (FileNotFoundException e) {
			throw new ParserException(e);
		} catch (Exception e) {
			log.error("Unexpected exception parsing file: {}", fileName, e);
			throw new ParserException(e);
		}
	}

	@Override
    public void doParse(InputStream input, String baseUri, RDFNotation notation, boolean keepBNode, RDFCallback callback) throws ParserException {
		this.callback = callback;
		try {
			switch(notation) {
				case NTRIPLES:
					parse(input, baseUri, Lang.NTRIPLES, keepBNode);
					break;
				case NQUAD:
					parse(input, baseUri, Lang.NQUADS, keepBNode);
					break;
				case RDFXML:
					parse(input, baseUri, Lang.RDFXML, keepBNode);
					break;
				case N3:
				case TURTLE:
					parse(input, baseUri, Lang.TURTLE, keepBNode);
					break;
				default:
					throw new NotImplementedException("Parser not found for format "+notation);
			}
		} catch (Exception e) {
			log.error("Unexpected exception.", e);
			throw new ParserException(e);
		}
	}

	 @Override
     public void start() {
             // TODO Auto-generated method stub

     }

     @Override
     public void triple(Triple parsedTriple) {
             triple.setAll(
					 JenaNodeFormatter.format(parsedTriple.getSubject()),
					 JenaNodeFormatter.format(parsedTriple.getPredicate()),
					 JenaNodeFormatter.format(parsedTriple.getObject()));
    	 callback.processTriple(triple, 0);
     }

     @Override
     public void quad(Quad quad) {
             triple.setAll(
					 JenaNodeFormatter.format(quad.getSubject()),
					 JenaNodeFormatter.format(quad.getPredicate()),
					 JenaNodeFormatter.format(quad.getObject()));
    	 callback.processTriple(triple, 0);
     }

     @Override
     public void base(String base) {
//           System.out.println("Base: "+base);
     }

     @Override
     public void prefix(String prefix, String iri) {
//           System.out.println("Prefix: "+prefix+" iri "+iri);
     }

     @Override
     public void finish() {
             // TODO Auto-generated method stub

     }


}
