/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testimport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.openrdf.model.URI;
import slib.graph.algo.accessor.GraphAccessor;
import slib.graph.algo.utils.GAction;
import slib.graph.algo.utils.GActionType;
import slib.graph.algo.validator.dag.ValidatorDAG;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.conf.GraphConf;
import slib.graph.io.loader.GraphLoaderGeneric;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Timer;

/**
 *
 * @author nacer
 */
public class Testimport {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SLIB_Exception  {
        // TODO code application logic here
        
        
        Timer t = new Timer();
        t.start();

        String ontoFile = "C:\\Users\\nacer\\Downloads\\travel-domain-populated.owl";

        URIFactory factory = URIFactoryMemory.getSingleton();
        URI graphURI = factory.getURI("http://graph/");
        G g = new GraphMemory(graphURI);

        
        GDataConf dataConf = new GDataConf(GFormat.RDF_XML, ontoFile);

        // We specify an action to root the vertices, typed as class without outgoing rdfs:subclassOf relationship 
        // Those vertices are linked to owl:Thing by an eddge x  rdfs:subClassOf owl:Thing 
        GAction actionRerootConf = new GAction(GActionType.REROOTING);

        // We now create the configuration we will specify to the generic loader
        GraphConf gConf = new GraphConf();
        gConf.addGDataConf(dataConf);
        gConf.addGAction(actionRerootConf);

        GraphLoaderGeneric.load(gConf, g);

        System.out.println(g.toString());

        Set<URI> roots = new ValidatorDAG().getTaxonomicRoots(g);
        System.out.println("Roots: " + roots);


        // We compute the similarity between two concepts 
        URI countryURI = factory.getURI("https://sites.google.com/site/portdial2/downloads-area/Travel-Domain.owl#Country");
        URI cityURI = factory.getURI("https://sites.google.com/site/portdial2/downloads-area/Travel-Domain.owl#City");

        // First we configure an intrincic IC 
        ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_DEPTH_MAX_NONLINEAR);
        // Then we configure the pairwise measure to use, we here choose to use Lin formula
        SMconf smConf = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998, icConf);

        // We define the engine used to compute the similarity
        SM_Engine engine = new SM_Engine(g);

        double sim = engine.compare(smConf, countryURI, cityURI);
        System.out.println("Similarity: " + sim);

        /* 
         * Notice that the first computation is expensive as the engine compute the IC and extra information 
         * which are cached by the engine
         * Let's perform numerous random computations (we only print some results).
         * We retrieve the set of vertices as a list
         */
        int totalComparison = 10;

        List<URI> listVertices = new ArrayList<URI>(GraphAccessor.getClasses(g));
       
        
        int nbConcepts = listVertices.size();
        int id1, id2;
        URI c1, c2;
        String idC1, idC2;
        Random r = new Random();

        for (int i = 0; i < totalComparison; i++) {
            id1 = r.nextInt(nbConcepts);
            id2 = r.nextInt(nbConcepts);

            c1 = listVertices.get(id1);
            c2 = listVertices.get(id2);

            sim = engine.compare(smConf, c1, c2);

//            if ((i + 1) % 1000 == 0) {
                idC1 = c1.getLocalName();
                idC2 = c2.getLocalName();

                System.out.println("Sim " + (i + 1) + "/" + totalComparison + "\t" + idC1 + "/" + idC2 + ": " + sim);
//            }
        }
        t.stop();
        t.elapsedTime();
    }
        
    }
    

