package Parser;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser 
{
	private static ArrayList<Statement> theListOfStatements = new ArrayList<Statement>();
	
	public static ArrayList<Statement> getParsedStatements()
	{
		return Parser.theListOfStatements;
	}
	
	public static void display()
	{
		for(Statement s : theListOfStatements)
		{
			System.out.println(s);
		}
	}
	
	static ResolveExpression parseResolve(String name)
	{
		//parse this string into language objects
		//turn remember syntax into a ResolveStatement
		ResolveExpression rs = new ResolveExpression(name);
		return rs;
	}
	
	private static boolean isMathOp(String s)
	{
		return "+-*/%".indexOf(s.trim()) > -1;
	}
	
	private static boolean isArithmeticBooleanOp(String s)
	{
		//only detect the first part of an arithmetic boolean 
		//op and then concatenate until space
		String[] theOps = "< > ! =".split(" ");
		for(int i = 0; i < theOps.length; i++)
		{
			if(theOps[i].equals(s.trim()))
			{
				return true;
			}
		}
		return false;
	}
	
	private static int getDoMathExpressionEndBucket(int startPos, String[] theParts)
	{
		//do-math do-math a + 7 + do-math b + 4
		int opCount = 0;
		while(startPos < theParts.length)
		{
			if(theParts[startPos].equals("do-math"))
			{
				opCount++;
			}
			else if(Parser.isMathOp(theParts[startPos]))
			{
				opCount--;
				if(opCount == 0)
				{
					return startPos-1; //add startPos to the end of the string
				}
			}
			startPos++;
		}
		return startPos;
		
	}
	
	static TestExpression parseTest(String expression)
	{
		//test do-math 5 + 4 < 7
		expression = expression.substring(5);
		String left = "";
		int pos;
		for(pos = 0; pos < expression.length(); pos++)
		{
			if(Parser.isArithmeticBooleanOp("" + expression.charAt(pos)))
			{
				break;
			}
			left = left + expression.charAt(pos);
		}
		
		//pos is now equal to the position of the beginning of the operator
		String op = "";
		while(expression.charAt(pos) != ' ')
		{
			op = op + expression.charAt(pos);
			pos++;
		}
		//op should now hold the entire operator
		
		String right = expression.substring(pos).trim();
		Expression leftExpression = Parser.parseExpression(left);
		Expression rightExpression = Parser.parseExpression(right);
		return new TestExpression(leftExpression, op, rightExpression);
	}
	
	static DoMathExpression parseDoMath(String expression)
	{
		//do-math do-math a + 7 + do-math b + 4 - doesn't work for this YET!
		//do-math expression op expression
		//make the above work for HW
		
		//do-math a + 7 - will work for this
		// (resolve expression a) + (int_lit expression 7)
		//right now we are assuming only a single level of do-math
		String[] theParts = expression.split("\\s+");
		Expression left;
		int pos = 1;
		String temp = "";
		if(theParts[pos].equals("do-math"))
		{
			//we need to handle the left expression as a do-math expression
			//left side contains at least 1 do-math expression
			//capture the substring from the current point until we reach the appropriate
			//operator
			pos = Parser.getDoMathExpressionEndBucket(0, theParts);
			//pos is the position in theParts where the do math is complete for the left side
			
			for(int i = 1; i <= pos; i++)
			{
				temp += theParts[i] + " ";
			}
			left = Parser.parseDoMath(temp.trim()); 
		}
		else
		{
			//it is either a resolve or literal expression
			left = Parser.parseExpression(theParts[pos]);
		}
		
		String math_op = theParts[pos+1];
		
		//everything from pos+2 forward is the right half of our do-math expression
	    temp = "";
		for(int i = pos+2; i < theParts.length; i++)
		{
			temp += theParts[i] + " ";
		}
		Expression right = Parser.parseExpression(temp.trim());
	
		//create and return an instance of DoMathExpression
		DoMathExpression theResult = new DoMathExpression(left, math_op, right);
		return theResult;
	}
	
	static LiteralExpression parseLiteral(String value)
	{
		//We ONLY have a single LiteralType - int literal
		return new Int_LiteralExpression(Integer.parseInt(value));
	}
	
	static RememberStatement parseRemember(String type, String name, Expression valueExpression)
	{
		//parse this string into language objects
		//turn remember syntax into a RememberStatement
		RememberStatement rs = new RememberStatement(type, name, valueExpression);
		return rs;
	}
	
	static WhileStatement parseWhile(Expression testExpression, Statement executeStatement)
	{
		WhileStatement ws = new WhileStatement(testExpression, executeStatement);
		return ws;
	}
	
	static WhileStatement parseWhile(Expression testExpression, ArrayList<Statement> executeStatements)
	{
		WhileStatement ws = new WhileStatement(testExpression, executeStatements);
		return ws;
	}
	
	static PrintStatement parsePrint(Expression expression_to_print)
	{
		PrintStatement ps = new PrintStatement(expression_to_print);
		return ps;
	}
	
	static UpdateStatement parseUpdate(String name, Expression valueExpression)
	{
		UpdateStatement us = new UpdateStatement(name, valueExpression);
		return us;
	}
	
	static QuestionStatement parseQuestion(TestExpression testExpression, Statement trueStatement, Statement falseStatement)
	{
		QuestionStatement qs = new QuestionStatement(testExpression, trueStatement, falseStatement);
		return qs;
	}
	
	public static void parse(String filename)
	{
		try
		{
			Scanner input = new Scanner(new File(System.getProperty("user.dir") + 
					"/src/" + filename));
			//builds a single string that has the contents of the file
			String fileContents = "";
			while(input.hasNext())
			{
				fileContents += input.nextLine().trim();
			}
			
			String[] theProgramLines = fileContents.split(";");
			for(int i = 0; i < theProgramLines.length; i++)
			{
				Parser.theListOfStatements.add(parseStatement(theProgramLines[i]));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.err.println("File Not Found!!!");
		}
	}
	
	static Expression parseExpression(String expression)
	{
		//determine which kind of expression this is, and parse it
		//right now we only have a single kind of expression (ResolveExpression)
		//Possible expressions types:
		// do-math, resolve, literal
		String[] theParts = expression.split("\\s+");
		if(theParts[0].equals("do-math"))
		{
			//must be a do-math expression
			return Parser.parseDoMath(expression);
		}
		else if(theParts[0].equals("test"))
		{
			//must be a test expression
			return Parser.parseTest(expression);
		}
		else if(Character.isDigit(theParts[0].charAt(0))) //does the value start with a number
		{
			//must a literal expression
			return Parser.parseLiteral(expression);
		}
		else
		{
			//must be a var name
			return Parser.parseResolve(expression);
		}
	}
	
	//parses the top level statements within our language
	static Statement parseStatement(String s)
	{
		//split the string on white space (1 or more spaces)
		String[] theParts = s.split("\\s+");
		// remember int b = do-math 5 + a;
		//s = "remember int a = 5"
		//parts = {"remember", "int", "a", "=", "5"}
		//s = "resolve a"
		//parts = {"resolve", "a"}
		
		if(theParts[0].equals("remember"))
		{
			int posOfEqualSign = s.indexOf('=');
			String everythingAfterTheEqualSign = s.substring(posOfEqualSign+1).trim();
	
			//parse a remember statement with type, name, and value
			return Parser.parseRemember(theParts[1], 
					theParts[2], Parser.parseExpression(everythingAfterTheEqualSign));
		}
		else if(theParts[0].equals("print"))
		{
			String temp = s.substring("print".length()).trim();
			Expression expression_to_print = Parser.parseExpression(temp);
			return Parser.parsePrint(expression_to_print);
		}
		else if(theParts[0].equals("while"))
		{   String tempp = s.substring("while".length()).trim();
			String[] tempPartss = tempp.split("do ");
			String test_expression_stringg = tempPartss[0].trim();
			
			
			//dec 6ss
			//System.out.println("tempp"+tempp);
			if(tempp.contains("while")) {
				String innerWhileFull = tempp.substring(tempp.indexOf("while"));
				
				//System.out.println("innerWhileFull"+innerWhileFull);
				
				String initialBlock = tempp.substring(0,tempp.indexOf("while"));
				
				//System.out.println("initialBlock"+initialBlock);
				
				
				
				ArrayList<Statement> stmts = new ArrayList<Statement>();
				
				String temp = initialBlock.substring("while".length()).trim();
				String allStatements = temp.substring(temp.indexOf("begin")+"begin".length(),temp.lastIndexOf(","));
				
				String[] stmParts = allStatements.split(", ");
				
				for(String stmStr:stmParts) {
					Statement blockStm = Parser.parseStatement(stmStr.trim());
					stmts.add(blockStm);
				}
				
				
				
				String finalStatement=innerWhileFull.substring(innerWhileFull.indexOf("end")+"end".length()+1,innerWhileFull.lastIndexOf("end"));
				//System.out.println("finalStatement "+finalStatement);
				
				Expression test_expression = Parser.parseExpression(test_expression_stringg);
				
				//inner while
				
				String innerWhile=innerWhileFull.substring(innerWhileFull.indexOf("while")+"while".length(),innerWhileFull.indexOf("end")+"end".length());
				//System.out.println("innerWhile "+innerWhile);
				
				if(innerWhile.contains("begin")) {//bansal begin
					
					ArrayList<Statement> stmtss = new ArrayList<Statement>();
					String temppp = innerWhile.substring("while".length()).trim();
					String allStatementss = temppp.substring(temppp.indexOf("begin")+"begin".length(),temppp.indexOf("end"));
					
					String[] stmPartss = allStatementss.split(", ");
					
					for(String stmStrr:stmPartss) {
						Statement blockStmm = Parser.parseStatement(stmStrr.trim());
						stmts.add(blockStmm);
					}
					
					String[] tempPartsss = innerWhile.split("do ");
					String test_expression_stringgg = tempPartsss[0].trim();
					Expression test_expressionn = Parser.parseExpression(test_expression_stringgg);
				//	return Parser.parseWhile(test_expressionn, stmts);
				}
				
				
				
				
				
				//inner while end
				
				//Final Part
				Statement finStm = Parser.parseStatement(finalStatement.trim());
				stmts.add(finStm);
				
				return Parser.parseWhile(test_expression, stmts);
				
				
				//String innerBlock = "";
				
				//if(innerWhileFull.contains("do")) {
				//	String allStatements = innerWhileFull.substring(temp.indexOf("begin")+"begin".length(),temp.indexOf("end"));
				//}
				
				//return null;
			}
			
		
			
			
			
			
			
			
			
			
			
			//
			
			if(s.contains("begin")) {//bansal begin
				
				ArrayList<Statement> stmts = new ArrayList<Statement>();
				String temp = s.substring("while".length()).trim();
				String allStatements = temp.substring(temp.indexOf("begin")+"begin".length(),temp.indexOf("end"));
				
				String[] stmParts = allStatements.split(", ");
				
				for(String stmStr:stmParts) {
					Statement blockStm = Parser.parseStatement(stmStr.trim());
					stmts.add(blockStm);
				}
				
				Expression test_expression = Parser.parseExpression(test_expression_stringg);
		
				return Parser.parseWhile(test_expression, stmts);
			}
			else {//bansal end
			
			//while <test-expression> do <statement>;
			String temp = s.substring("while".length()).trim();
			String[] tempParts = temp.split("do ");
			String test_expression_string = tempParts[0].trim();
			String execute_statement_string = tempParts[1].trim();
			Expression test_expression = Parser.parseExpression(test_expression_string);
			Statement execute_statement = Parser.parseStatement(execute_statement_string);
			return Parser.parseWhile(test_expression, execute_statement);
			}
			
			
			
		}
		else if(theParts[0].equals("update"))
		{
			String temp = s.substring("update".length()).trim();
			String[] tempParts = temp.split("=");
			String varName = tempParts[0].trim();
			String expressionString = tempParts[1].trim();
			Expression theExpression = Parser.parseExpression(expressionString);
			return Parser.parseUpdate(varName, theExpression);
		}
		else if(theParts[0].equals("question"))
		{
			String expression = s.substring("question".length()).trim();
			int posOfDoKeyword = expression.indexOf("do");
			String testExpression = expression.substring(0, posOfDoKeyword);
			expression = expression.substring(posOfDoKeyword + "do".length()).trim();
			int posOfOtherwiseKeyword = expression.indexOf("otherwise");
			String trueStatement = expression.substring(0, posOfOtherwiseKeyword).trim();
			String falseStatement = expression.substring(posOfOtherwiseKeyword + "otherwise".length()).trim();
			
			return Parser.parseQuestion(
							(TestExpression)Parser.parseExpression(testExpression), 
							Parser.parseStatement(trueStatement), 
							Parser.parseStatement(falseStatement));
			
		}
		throw new RuntimeException("Not a known statement type: " + s);
	}
}
