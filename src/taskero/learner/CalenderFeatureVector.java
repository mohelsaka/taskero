package taskero.learner;

public class CalenderFeatureVector {

	int[]featureVector;
	private int numOfFeatures=22;
	/**
	 * @param args
	 */
	public CalenderFeatureVector()
	{
		featureVector=new int[numOfFeatures];
	}
	
	public void setFeatureVector(int []feature)
	{
		this.featureVector=feature;
	}
	
//	"3 qid:1 1:0 2:1 3:0 4:0.2 5:0\n\0"
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder feature=new StringBuilder();
		for(int i=0;i<featureVector.length;i++)
		{
			feature.append(" "+(i+1)+":"+featureVector[i]);
		}
		return feature.toString();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
