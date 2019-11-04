package controller.TopFile;

/*******************************************************************************
 *
 *	Filename   :	TopFileLoader.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Abstract class. Should be extended by all implemented topology file
 *	loaders. Single method: loadTopFile(). Method should return a
 *	"TopologyFile" object.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/

public abstract class TopFileLoader {





    public TopFileLoader(){

    }

    public abstract TopologyFile loadTopFile(String path);

}
