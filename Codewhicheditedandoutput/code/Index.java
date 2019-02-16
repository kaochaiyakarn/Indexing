/*
 * Chaiyakarn khanan 5988130
 * Chanwit Panleng 5988076
 * Khachen Hempatawee 5988047
 * */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Index {

	// Term id -> (position in index file, doc frequency) dictionary
	private static Map<Integer, Pair<Long, Integer>> postingDict 
		= new TreeMap<Integer, Pair<Long, Integer>>();
	// Doc name -> doc id dictionary
	private static Map<String, Integer> docDict
		= new TreeMap<String, Integer>();
	// Term -> term id dictionary
	private static Map<String, Integer> termDict
		= new TreeMap<String, Integer>();
	// Block queue
	private static LinkedList<File> blockQueue
		= new LinkedList<File>();

	// Total file counter
	private static int totalFileCount = 0;
	// Document counter
	private static int docIdCounter = 0;
	// Term counter
	private static int wordIdCounter = 0;
	// Index
	private static BaseIndex index = null;

	private static long sPoint=0;
	/* 
	 * Write a posting list to the given file 
	 * You should record the file position of this posting list
	 * so that you can read it back during retrieval
	 * 
	 * */
	private static void writePosting(FileChannel fc, PostingList posting)
			throws IOException {
		/*
		 * TODO: Your code here
		 *	 
		 */
		int frequency=posting.getList().size();
		int termId = posting.getTermId();
		//System.out.println("Before postingdict"+postingDict);
		index.writePosting(fc,posting);
		Pair<Long, Integer > order = new Pair<Long, Integer>(sPoint,frequency);
		postingDict.put(termId, order);
		//System.out.println(frequency);
		//System.out.println(fc.size());
		sPoint += 8+ (frequency*4); // check between fc.position;
		//System.out.println("After postingDict"+postingDict);
		//System.out.println("posting "+sPoint);
		
	}
	

	 /**
     * Pop next element if there is one, otherwise return null
     * @param iter an iterator that contains integers
     * @return next element or null
     */
    private static Integer popNextOrNull(Iterator<Integer> iter) {
        if (iter.hasNext()) {
            return iter.next();
        } else {
            return null;
        }
    }
	
    
   
	
	/**
	 * Main method to start the indexing process.
	 * @param method		:Indexing method. "Basic" by default, but extra credit will be given for those
	 * 			who can implement variable byte (VB) or Gamma index compression algorithm
	 * @param dataDirname	:relative path to the dataset root directory. E.g. "./datasets/small"
	 * @param outputDirname	:relative path to the output directory to store index. You must not assume
	 * 			that this directory exist. If it does, you must clear out the content before indexing.
	 */
	public static int runIndexer(String method, String dataDirname, String outputDirname) throws IOException 
	{
		/* Get index */
		String className = method + "Index";
		try {
			Class<?> indexClass = Class.forName(className);
			index = (BaseIndex) indexClass.newInstance();
		} catch (Exception e) {
			System.err
					.println("Index method must be \"Basic\", \"VB\", or \"Gamma\"");
			throw new RuntimeException(e);
		}
		
		/* Get root directory */
		File rootdir = new File(dataDirname);
		if (!rootdir.exists() || !rootdir.isDirectory()) {
			System.err.println("Invalid data directory: " + dataDirname);
			return -1;
		}
		
		   
		/* Get output directory*/
		File outdir = new File(outputDirname);
		if (outdir.exists() && !outdir.isDirectory()) {
			System.err.println("Invalid output directory: " + outputDirname);
			return -1;
		}
		
		/*	TODO: delete all the files/sub folder under outdir
		 * 
		 */
		
		delete(outdir);
		if (!outdir.exists()) {
			if (!outdir.mkdirs()) {
				System.err.println("Create output directory failure");
				return -1;
			}
		}
		
		
		
		
		/* BSBI indexing algorithm */
		File[] dirlist = rootdir.listFiles();

		wordIdCounter =0;
		
		/* For each block */
		for (File block : dirlist) {
			Map<Integer,PostingList> posting = new TreeMap<Integer,PostingList>();//id+docid				
			File blockFile = new File(outputDirname, block.getName());
			System.out.println("Processing block "+block.getName());
			blockQueue.add(blockFile);

			File blockDir = new File(dataDirname, block.getName());
			File[] filelist = blockDir.listFiles();
			
			/* For each file */
			for (File file : filelist) {
				++totalFileCount;
				String fileName = block.getName() + "/" + file.getName();
				
				 // use pre-increment to ensure docID > 0
                int docId = ++docIdCounter;
                docDict.put(fileName, docId);
				

				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					String[] tokens = line.trim().split("\\s+");
					for (String token : tokens) {
						/*
						 * TODO: Your code here
						 *       For each term, build up a list of
						 *       documents in which the term occurs
						 */
						if(!termDict.containsKey(token)){
							wordIdCounter++;	
							termDict.put(token, wordIdCounter);
						}
						if(posting.containsKey(termDict.get(token))){
							if(!posting.get(termDict.get(token)).getList().contains(docId)){  //checkkkk  no ! will be add same word on it
								posting.get(termDict.get(token)).getList().add(docId);
							}
						}
						else if(!posting.containsKey(termDict.get(token))){
							PostingList newPost = new PostingList(termDict.get(token));
							newPost.getList().add(docId);
							posting.put(termDict.get(token), newPost);
						}
						
						
					}
				}
				reader.close();
			}

			/* Sort and output */
			if (!blockFile.createNewFile()) {
				System.err.println("Create new block failure.");
				return -1;
			}
			
			RandomAccessFile bfc = new RandomAccessFile(blockFile, "rw");
			
			/*
			 * TODO: Your code here
			 *       Write all posting lists for all terms to file (bfc) 
			 */
			FileChannel fileCh = bfc.getChannel();
			int specialcheck=0;
			for(int i: posting.keySet()){
//				System.out.println("check");
				specialcheck=1;
				writePosting(fileCh, posting.get(i));
			}
			if(specialcheck==1){
				System.out.println("check");
			}
			bfc.close();
		}

		/* Required: output total number of files. */
		//System.out.println("Total Files Indexed: "+totalFileCount);

		/* Merge blocks */
		while (true) {
			if (blockQueue.size() <= 1)
				break;

			File b1 = blockQueue.removeFirst();
			File b2 = blockQueue.removeFirst();
			
			File combfile = new File(outputDirname, b1.getName() + "+" + b2.getName());
			if (!combfile.createNewFile()) {
				System.err.println("Create new block failure.");
				return -1;
			}

			RandomAccessFile bf1 = new RandomAccessFile(b1, "r");
			RandomAccessFile bf2 = new RandomAccessFile(b2, "r");
			RandomAccessFile mf = new RandomAccessFile(combfile, "rw");
			 
			/*
			 * TODO: Your code here
			 *       Combine blocks bf1 and bf2 into our combined file, mf
			 *       You will want to consider in what order to merge
			 *       the two blocks (based on term ID, perhaps?).
			 *       
			 */
			//bytebuffer
			
			int checktest =0;
			//setposition 
			long position =0; //check to use file.getposition
			PostingList posRead = index.readPosting(bf1.getChannel().position(position));
			Map<Integer,PostingList> postingMerge = new TreeMap<Integer,PostingList>();//id+docid
			//System.out.println(posRead);
			while(posRead!=null){
//				if(checktest==0){
//					posRead = index.readPosting(bf1.getChannel().position(position));
//					checktest++;
//				}
				postingMerge.put(posRead.getTermId(),posRead);// input data to treeMap from 1 then add 2 to it
				//position+=8+bf1.length();
				//System.out.println("position ="+position );
				position += 8+(4*posRead.getList().size());
				//System.out.println("position ="+position );
				posRead = index.readPosting(bf1.getChannel().position(position));
			}
			
			//System.out.println("Position check "+position);
			position = 0;
			//int positionbf2=0;
			posRead = index.readPosting(bf2.getChannel().position(position));
			while(posRead!=null){
				if(postingMerge.containsKey(posRead.getTermId())){
					for(int i: posRead.getList()){
						if(!postingMerge.get(posRead.getTermId()).getList().contains(i)){
							postingMerge.get(posRead.getTermId()).getList().add(i);
						}
					}
					
				}
				else {
					postingMerge.put(posRead.getTermId(),posRead);
				}
//				if(postingMerge.containsKey(posRead.getTermId())){
//					for(int i: posRead.getList()){
//						if(!postingMerge.get(posRead.getTermId()).getList().contains(i)){
//							postingMerge.get(posRead.getTermId()).getList().add(i);
//						}
//					}
//				}
//				else{
//					postingMerge.put(posRead.getTermId(),posRead);
//				}
				position += 8+(4*posRead.getList().size());
				posRead = index.readPosting(bf2.getChannel().position(position));
				
			}
			sPoint = 0;
			for(int i: postingMerge.keySet()){
				Collections.sort(postingMerge.get(i).getList());
				writePosting(mf.getChannel(), postingMerge.get(i));
			}
			
			
			
			
			bf1.close();
			bf2.close();
			mf.close();
			b1.delete();
			b2.delete();
			blockQueue.add(combfile);
		}

		/* Dump constructed index back into file system */
		File indexFile = blockQueue.removeFirst();
		indexFile.renameTo(new File(outputDirname, "corpus.index"));

		BufferedWriter termWriter = new BufferedWriter(new FileWriter(new File(
				outputDirname, "term.dict")));
		for (String term : termDict.keySet()) {
			termWriter.write(term + "\t" + termDict.get(term) + "\n");
		}
		termWriter.close();

		BufferedWriter docWriter = new BufferedWriter(new FileWriter(new File(
				outputDirname, "doc.dict")));
		for (String doc : docDict.keySet()) {
			docWriter.write(doc + "\t" + docDict.get(doc) + "\n");
		}
		docWriter.close();

		BufferedWriter postWriter = new BufferedWriter(new FileWriter(new File(
				outputDirname, "posting.dict")));
		for (Integer termId : postingDict.keySet()) {
			postWriter.write(termId + "\t" + postingDict.get(termId).getFirst()
					+ "\t" + postingDict.get(termId).getSecond() + "\n");
		}
		postWriter.close();
		
		return totalFileCount;
	}

	private static void delete(File deleteFile) {
		// TODO Auto-generated method stub
		if(deleteFile.isFile()){
			for(File fi : deleteFile.listFiles()){
				fi.delete();
				//delete(fi);
			}
		}
	}


	public static void main(String[] args) throws IOException {
		/* Parse command line */
		if (args.length != 3) {
			System.err
					.println("Usage: java Index [Basic|VB|Gamma] data_dir output_dir");
			return;
		}

		/* Get index */
		String className = "";
		try {
			className = args[0];
		} catch (Exception e) {
			System.err
					.println("Index method must be \"Basic\", \"VB\", or \"Gamma\"");
			throw new RuntimeException(e);
		}

		/* Get root directory */
		String root = args[1];
		

		/* Get output directory */
		String output = args[2];
		runIndexer(className, root, output);
	}
	

}

