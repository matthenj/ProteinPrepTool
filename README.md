# ProteinPrepTool
Java applet for processing biomolecular trajectories ready for use with Haptimol FlexiDock (www.haptimol.co.uk) 

ProteinPrepTool (PPT) is intended to improve the accessibility of Haptimol FlexiDock. The application can read and interpret protein database files, topologies and trajectories, and output a set of files compatible with Haptimol FlexiDock.

Current Capabilities 
- PPT can ensure compatibility of PDB and TOP files with Haptimol FlexiDock. 
- PPT can convert PRMTOP and PSF topology files into files compatible with Haptimol FlexiDock. (NOTE :- these files contain all of    the information required for use with Haptimol FlexiDock; Using these files with other applications is not supported, and may not produce the expected results).  
- PPT can read in a biomolecular trajectory, and compute the eigenvalues/vectors of that trajectory. These values are output in CSV format. (Note :- this may take a long time)


Compatible file formats:
- Protein structure files: *.PDB
- Protein Topology files: *.TOP, *.PSF, *.PRMTOP
- Protein Trajectory files: *.NETCDF

Known Current Limitations
- Configure window not working: This means it is not possible to skip steps during processing. The intention is to allow the user to customise the extent of PPTs processing: If they already have the protein eigenvalues available, there is no sense in performing the computation again. 
- Sometimes the application does not exit on completion. Some thread stuff going on here.
