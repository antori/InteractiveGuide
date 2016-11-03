/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neo4jimporter;

import java.io.File;

/**
 *
 * @author Marco
 */
public class Neo4jImporter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        Neo4jQueryExecutor n = new Neo4jQueryExecutor();
        n.createPaintingsDatabase("C:" + File.separator + "Users" + File.separator + "Marco"
				+ File.separator + "Downloads" + File.separator + "data.xml");
    }
    
}
