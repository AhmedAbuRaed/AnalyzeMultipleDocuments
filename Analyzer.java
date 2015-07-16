package edu.upf.abletoinclude.analyzer;


import gate.AnnotationSet;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;

import java.io.File;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import div.nlp.parser.MateParser;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;





	/**
	* The class Analyzer uses a set of Processing Resources from GATE and the mate-tool
	* parser (through MateParserGATEplugin software package) for the analysis of documents in 
	* English.
	* 
	* @author  TALN-UPF
	* 
	**/

public class Analyzer {
	private SerialAnalyserController pipeline =  null;
	private String strURLMateParserModelsPath="";
	private boolean flagUseMateParser=true;
	
	
	// Processing Resources
	private LanguageAnalyser annotation_delete = null;
	private LanguageAnalyser tokenizer = null;
	private LanguageAnalyser gazetteer = null;
	private LanguageAnalyser sentence_splitter = null;
	private LanguageAnalyser pos_tagger = null;
	private LanguageAnalyser morpho = null;
	private LanguageAnalyser ne_transducer = null;
	private LanguageAnalyser ortho_matcher = null;
	private LanguageAnalyser mate_parser = null;


	
	
	/**
	 * Analyzer class constructor.
	 * @param  flagUseMateParser  a boolean that indicates if the MateParser is used or not.
	 * @param  strURLMateParserModelsPath the URL/Path to the the location of the MateParser models
	 */

	public  Analyzer(boolean flagUseMateParser, String strURLMateParserModelsPath) throws GateException, MalformedURLException
	{

		this.flagUseMateParser = flagUseMateParser;
		
		if (flagUseMateParser==true)
		{
			strURLMateParserModelsPath = strURLMateParserModelsPath;
		}
		
		Gate gate = new Gate();
		
		gate.init(); // prepare the library
		
		// get the root plugins dir
		File pluginsDir = Gate.getPluginsHome();	
		
		// ANNIE plugin
		File anniePluginDir = new File(pluginsDir, "ANNIE");
		
		// Tools plugin
		File toolsPluginDir = new File(pluginsDir, "Tools");
		
		
		// loading the GATE plugins.
		Gate.getCreoleRegister().registerDirectories(anniePluginDir.toURI().toURL());
		Gate.getCreoleRegister().registerDirectories(toolsPluginDir.toURI().toURL());
		
		// loading the MateParser GATE plugin.
		Gate.getCreoleRegister().registerComponent(MateParser.class); 
		
		
		
		// Build the pipeline
		pipeline =  (SerialAnalyserController)Factory.createResource("gate.creole.SerialAnalyserController");
		
	

		// Creating the Processing Resources 
		annotation_delete = (LanguageAnalyser) Factory.createResource("gate.creole.annotdelete.AnnotationDeletePR");
		pipeline.add(annotation_delete);
				
		tokenizer = (LanguageAnalyser) Factory.createResource("gate.creole.tokeniser.DefaultTokeniser");
		pipeline.add(tokenizer);
		
		gazetteer = (LanguageAnalyser) Factory.createResource("gate.creole.gazetteer.DefaultGazetteer");
		pipeline.add(gazetteer);
		
		sentence_splitter = (LanguageAnalyser) Factory.createResource("gate.creole.splitter.SentenceSplitter");
		pipeline.add(sentence_splitter);
		
		pos_tagger = (LanguageAnalyser) Factory.createResource("gate.creole.POSTagger");
		pipeline.add(pos_tagger);
		
		morpho = (LanguageAnalyser)Factory.createResource("gate.creole.morph.Morph");
		pipeline.add(morpho);
		
		ne_transducer = (LanguageAnalyser) Factory.createResource("gate.creole.ANNIETransducer");
		pipeline.add(ne_transducer);
		
		ortho_matcher = (LanguageAnalyser) Factory.createResource("gate.creole.orthomatcher.OrthoMatcher");
		pipeline.add(ortho_matcher);
		

		
		if (flagUseMateParser==true)
		{
			String lemmatizerModelURL = strURLMateParserModelsPath + "/"+"CoNLL2009-ST-English-ALL.anna-3.3.lemmatizer.model";
			String POStaggerModelURL = strURLMateParserModelsPath + "/"+"CoNLL2009-ST-English-ALL.anna-3.3.postagger.model";
			String parserModelURL = strURLMateParserModelsPath + "/"+"CoNLL2009-ST-English-ALL.anna-3.3.parser.model";
			
			
			FeatureMap mate_parser_features = Factory.newFeatureMap();
			mate_parser_features.put("lemmatizerModel",lemmatizerModelURL);
			mate_parser_features.put("taggerModel",POStaggerModelURL);
			mate_parser_features.put("parserModel",parserModelURL);
			mate_parser_features.put("executeMorphoAnalysis","true");
			mate_parser_features.put("executeDepParsing","true");
		    
		    mate_parser = (LanguageAnalyser) Factory.createResource(MateParser.class.getCanonicalName(),mate_parser_features);
		    pipeline.add(mate_parser);
		}
	    
		
	}
	

	
	
	
	/**
	 * Sets the MateParser Models path.
	 * 
	 * @param  strURLMateParserModelsPath the URL/Path to the the location of the MateParser models
	 */

	public void setURLMateParserModelsPath (String strURLMateParserModelPath)
	{
		strURLMateParserModelsPath = strURLMateParserModelPath;
		
	}
	

	
	/**
	 * Sets the corpus to analyze.
	 * 
	 * @param  corpus The corpus to be analyzed
	 */
	
	public void setCorpus(Corpus corpus) throws ExecutionException
	{
		pipeline.setCorpus(corpus);
	}
	
	
	/**
	 *  Executes the pipeline and analyzes the input corpus.
	 */
	
	public void execute() throws ExecutionException
	{
		pipeline.execute();
	}
	
	
	
	/**
	 *  Deletes the GATE processing resources.
	 */
	
	protected void finalize( ) throws Throwable
	{
		annotation_delete.cleanup();
		tokenizer.cleanup();
		gazetteer.cleanup();
		sentence_splitter.cleanup();
		pos_tagger.cleanup();
		morpho.cleanup();
		ne_transducer.cleanup();
		ortho_matcher.cleanup();
		
		
		if (flagUseMateParser==true)
		{
			mate_parser.cleanup();
		}
		
		
		Factory.deleteResource(annotation_delete);
		Factory.deleteResource(tokenizer);
		Factory.deleteResource(gazetteer);
		Factory.deleteResource(sentence_splitter);
		Factory.deleteResource(pos_tagger);
		Factory.deleteResource(morpho);
		Factory.deleteResource(ne_transducer);
		Factory.deleteResource(ortho_matcher);
		
		
		if (flagUseMateParser==true)
		{
			Factory.deleteResource(mate_parser);
		}
			
		pipeline.remove(annotation_delete);
		pipeline.remove(tokenizer);
		pipeline.remove(gazetteer);
		pipeline.remove(sentence_splitter);
		pipeline.remove(pos_tagger);
		pipeline.remove(morpho);
		pipeline.remove(ne_transducer);
		pipeline.remove(ortho_matcher);
		
		
		if (flagUseMateParser==true)
		{
			pipeline.remove(mate_parser);
		}
		
		pipeline.cleanup();
		Factory.deleteResource(pipeline);

		
	}
	
	
	
	/**    Analyzer Standalone program.
	  *    Uses the Analyzer class to analyze files and URLs. 
	  **/
	
	/*public static void main(String[] args)	throws Exception{

		
			CommandLineParser parser = new BasicParser();
			Options options = new Options();
			
			Option optionHelp = new Option( "help", "print this message" );
			Option optionURLMateParserModels = OptionBuilder.withArgName("URL")
												.hasArg()
												.withDescription("URL of the MateParser Models Path. (only complete PATHs no relative PATHS). (option not necessary if -d option is active)" )
												.create( "urlmodels" );
			
			Option optionURLInputFile = OptionBuilder.withArgName("URL")
											.hasArg()
											.withDescription("URL of the input file in txt format. (only complete PATHs no relative PATHS)" )
											.create( "urlinput" );
			
			
			Option optionStrOutputFile = OptionBuilder.withArgName("file")
											.hasArg()
											.withDescription("file name of the output file in XML format" )
											.create( "outputfile" );
			
			
			options.addOption( optionHelp );
			
			options.addOption( optionURLInputFile );
			options.addOption( optionStrOutputFile );
			options.addOption( optionURLMateParserModels );
			options.addOption( optionURLMateParserModels );
			options.addOption( "v", "verbosity", false, "Print the analyzed Annotations and debug info." );
			options.addOption( "d", "disableparser", false, "disables the MateParser dependencies info." );
			
			
			final CommandLine cmd = parser.parse( options, args);

			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			
			boolean verbosity=false;
			String baseModelPath="";
			String urlInputFile="";
			String strOutputFile="";
			
			boolean mateparser = true;
			
			if (cmd.hasOption("d"))					
			{	
				
				mateparser = false;
				


			}
			else
			{
				
				if (cmd.hasOption("urlmodels"))	
				{	 
					baseModelPath = cmd.getOptionValue("urlmodels");	
				}
				else												
				{	
					formatter.printHelp( "Analyzer", options );	
					System.exit(-1);
				}	
			}
			
			
			
			if (cmd.hasOption("urlinput"))						
			{	 
				urlInputFile = cmd.getOptionValue("urlinput");	
			}
			else												
			{	 
				formatter.printHelp( "Analyzer", options );						
				System.exit(-1);
			}	

			if (cmd.hasOption("outputfile"))					
			{	
				strOutputFile = cmd.getOptionValue("outputfile");	
			}
			
			else
			{	 
				formatter.printHelp( "Analyzer", options );				
				System.exit(-1);
			}	
			
			if (cmd.hasOption("v"))					
			{	
				verbosity = true;
			}
			
			
			
		
			System.out.println("Input URL: " +urlInputFile);
			System.out.println("Output XML file: " + strOutputFile);
			System.out.println("base models: " +baseModelPath);

			
			
			
			Analyzer analyzer = new Analyzer(mateparser,baseModelPath);
			
			
			
			
			System.out.println("Input URL: " +urlInputFile);
			System.out.println("Output XML file: " + strOutputFile);
			Corpus corpus = Factory.newCorpus("corpus");
			
			
			Document doc1 = Factory.newDocument(new URL(urlInputFile));
			corpus.add(doc1);
			
			analyzer.setCorpus(corpus);
			analyzer.execute();
			
			
			Iterator<Document> iteratorDocuments = corpus.iterator();
			
			while (iteratorDocuments.hasNext())
			{
				Document documentActual = (Document) iteratorDocuments.next();
				if (verbosity==true)
				{	
					System.out.println("Found annotations of the following types: " + documentActual.getAnnotations().toString());
					
				}
			}
					
			
			// Print the annotations in a file
			PrintWriter pw=new PrintWriter(new File(strOutputFile));
			pw.println(doc1.toXml());
            pw.flush();
            pw.close();
			
	}
*/
    public static void main(String[] args)	throws Exception{


        CommandLineParser parser = new BasicParser();
        Options options = new Options();

        Option optionHelp = new Option( "help", "print this message" );
        Option optionURLMateParserModels = OptionBuilder.withArgName("URL")
                .hasArg()
                .withDescription("URL of the MateParser Models Path. (only complete PATHs no relative PATHS). (option not necessary if -d option is active)" )
                .create( "urlmodels" );

        Option optionStrInputDir = OptionBuilder.withArgName("file")
                .hasArg()
                .withDescription("URL of the input directory in txt format. (only complete PATHs no relative PATHS)" )
                .create( "strinputdir" );


        Option optionStrOutputDir = OptionBuilder.withArgName("file")
                .hasArg()
                .withDescription("URL of the output directory in text format" )
                .create( "stroutputdir" );


        options.addOption( optionHelp );

        options.addOption( optionStrInputDir );
        options.addOption( optionStrOutputDir );
        options.addOption( optionURLMateParserModels );
        options.addOption( optionURLMateParserModels );
        options.addOption( "v", "verbosity", false, "Print the analyzed Annotations and debug info." );
        options.addOption( "d", "disableparser", false, "disables the MateParser dependencies info." );


        final CommandLine cmd = parser.parse( options, args);

        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();

        boolean verbosity=false;
        String baseModelPath="";
        String strInputDir= null;
        String strOutputDir= null;

        boolean mateparser = true;

        if (cmd.hasOption("d"))
        {

            mateparser = false;



        }
        else
        {

            if (cmd.hasOption("urlmodels"))
            {
                baseModelPath = cmd.getOptionValue("urlmodels");
            }
            else
            {
                formatter.printHelp( "Analyzer", options );
                System.exit(-1);
            }
        }



        if (cmd.hasOption("strinputdir"))
        {
            strInputDir = cmd.getOptionValue("strinputdir");
        }
        else
        {
            formatter.printHelp( "Analyzer", options );
            System.exit(-1);
        }

        if (cmd.hasOption("stroutputdir"))
        {
            strOutputDir = cmd.getOptionValue("stroutputdir");
        }

        else
        {
            formatter.printHelp( "Analyzer", options );
            System.exit(-1);
        }

        if (cmd.hasOption("v"))
        {
            verbosity = true;
        }




        System.out.println("Input Dir: " +strInputDir);
        System.out.println("Output Dir: " + strOutputDir);
        System.out.println("base models: " +baseModelPath);




        Analyzer analyzer = new Analyzer(mateparser,baseModelPath);




        System.out.println("Input Dir : " +strInputDir);
        System.out.println("Output Dir : " + strOutputDir);
        Corpus corpus = Factory.newCorpus("corpus");

        File inputfolder = new File(strInputDir);
        URL tempurl;
        for (final File fileEntry : inputfolder.listFiles()) {
            Document doc1 = Factory.newDocument(fileEntry.toURI().toURL());
            corpus.add(doc1);

            analyzer.setCorpus(corpus);
            analyzer.execute();






            Iterator<Document> iteratorDocuments = corpus.iterator();

            while (iteratorDocuments.hasNext())
            {
                Document documentActual = (Document) iteratorDocuments.next();
                if (verbosity==true)
                {
                    System.out.println("Found annotations of the following types: " + documentActual.getAnnotations().toString());

                }
            }
            int lastDot = fileEntry.getName().lastIndexOf(".");
            // Print the annotations in a file
            PrintWriter pw=new PrintWriter(new File(strOutputDir + "/" + fileEntry.getName().substring(0, lastDot) + ".xml"));
            pw.println(doc1.toXml());
            pw.flush();
            pw.close();

            Factory.deleteResource(doc1);
        }




    }
}
