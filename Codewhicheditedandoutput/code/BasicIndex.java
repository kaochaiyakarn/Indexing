/*
 * Chaiyakarn khanan 5988130
 * Chanwit Panleng 5988076
 * Khachen Hempatawee 5988047
 * */
import java.awt.List;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


public class BasicIndex implements BaseIndex {
	@Override
	public PostingList readPosting(FileChannel fc) {
		/*
		 * TODO: Your code here
		 *       Read and return the postings list from the given file.
		 */
		int WID,frequencycheck;
		ByteBuffer bfunction = ByteBuffer.allocate(4);
		PostingList simulatePosting = null;
		try {
			if(fc.size()>fc.position()){  
				int RBy = fc.read(bfunction);
				bfunction.flip(); // cant remove dont forget
				WID = bfunction.getInt();
				bfunction.clear();
				simulatePosting = new PostingList(WID);
				RBy = fc.read(bfunction);
				bfunction.flip();
				frequencycheck = bfunction.getInt();
				bfunction.clear();
				int sizeLoop = frequencycheck;
				for(int i=0; i<sizeLoop; i++)
				{
					RBy = fc.read(bfunction);
					bfunction.flip();
					int docId = bfunction.getInt();
					simulatePosting.getList().add(docId);
					bfunction.clear();
				}
			}
			else{
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	return simulatePosting;
	}

	@Override
	public void writePosting(FileChannel fc, PostingList p){
		/*
		 * TODO: Your code here
		 *       Write the given postings list to the given file.
		 */
//		ByteBuffer bfunction = ByteBuffer.allocate(4+4+(4*p.getList().size()));
//		bfunction.putInt(p.getTermId());
//		bfunction.putInt(p.getList().size());
//		for(int docId: p.getList()){
//			bfunction.putInt(docId);
//		}
//		
//			bfunction.flip();
//			fc.write(bfunction);
//			bfunction.clear();
		ArrayList<Integer> check = new ArrayList<Integer>();
		ArrayList<Integer> num = new ArrayList<Integer>();
		ByteBuffer bfunction = ByteBuffer.allocate(1048576);
//		check.add(p.getTermId());
//		check.add(p.getList().size());
//		for(int i=0;i<100;i++){
//			System.out.println("check:"+check.get(i)); 
//		}
		num.add(p.getTermId());
		num.add(p.getList().size());
//		for(int i=0;i<num.size();i++){
//			System.out.println(num.get(i));
//		}
		for(int i = 0; i < p.getList().size(); i++){
			num.add(p.getList().get(i));
		}
		int j=num.size();
		Integer[] gS = new Integer[j];
		gS = num.toArray(gS);
		//System.out.println("Check size = " + getSize);
		for(int i = 0; i < gS.length; i++){
			bfunction.putInt(gS[i]);
		}
		bfunction.flip(); // cant remove wtf
		try {
			fc.write(bfunction);
			//System.out.println(bfunction);
			//System.out.println("check basicindex");
			bfunction.clear();

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("BASICINDEX LINE 92");
		}


	}
}

