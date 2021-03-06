/**
 * Copyright (c) 2016-2017, the original author or authors (dysd_2016@163.com).
 * <p>
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/gpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dysd.dao.mybatis.schema.function;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dysd.dao.exception.DaoExceptionCodes;
import org.dysd.dao.mybatis.schema.SchemaHandlers;
import org.dysd.util.Tool;
import org.dysd.util.exception.Throw;
import org.dysd.util.logger.CommonLogger;

/**
 * SQL配置函数解析帮助
 * @author linjisong
 * @version 0.0.1
 * @date 2016-11-12
 */
public class SqlConfigFunctionParser {
	
	/**
	 * 设别SQL配置函数的正则表达式，匹配形如$fn_name{args}的配置
	 */
	private static final String pattern = new StringBuilder()
			.append("(?<=\\s+|^)")//肯定逆序环视(?<=...)，这里表示前面为开头或空白字符
			.append("\\$") // 转义为"$"符号本身，和下面一起表示以$开头，并且紧跟（不能出现空白字符）一个字母或下划线开头的字母数字下划线组合
				.append("(") //捕获型分组1开始，用于捕获形似$name{args}里面的名称name
					.append("[_a-z](?:[_0-9a-z])*") //一个字母或下划线开头的字母数字下划线组合，其中 (?:...)表示非捕获型分组
				.append(")")//捕获型分组1结束
			.append("\\s*\\{\\s*")//表示"{"字符，前后可以有任意空白字符
				.append("(") //捕获型分组2开始，用于捕获形似$name{args}里面的参数args
					.append("([^{}]|") //这里的括号也会捕获，但是不会影响捕获组序号，表达式表示非"{"|"}"字符，或者--
						.append("((\\$|\\#)\\{[^{}]+\\})") //或者"${"|"#{"开头，中间不是"{"|"}"的字符，最后跟一个"}"字符
														   //也即允许args里面出现${...}和#{...}
					.append(")*")//表示上面两种字符可以出现任意次
				.append(")")//捕获型分组2结束
			.append("\\s*\\}")//表示"}"字符，前面任意空白字符
			.append("(?=\\s+|$)")//肯定顺序环视(?=...)，这里表示后面为结尾或空白字符
			.toString();
	private static final Pattern sqlConfigFunctionPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

	/**
	 * 根据数据库id执行Sql配置函数
	 * @param databaseId
	 * @param context
	 * @return
	 */
	public static String evalSqlConfigFunction(String databaseId, String context){
		Matcher matcher = sqlConfigFunctionPattern.matcher(context);
		boolean debug = CommonLogger.isDebugEnabled();
		while(matcher.find()){
			String expression = matcher.group(0);
			String name = matcher.group(1);
			String args = matcher.group(2);
			String replacement = resolveSqlConfigFunction(databaseId, name, resolveArgs(args));
			String after = matcher.replaceFirst(Matcher.quoteReplacement(replacement));
			if(debug){
				StringBuffer sb = new StringBuffer("\nSqlConfigFunction eval ...");
				sb.append("\n\tmatcher context    : ").append(context)
				  .append("\n\tmatcher expression : ").append(expression)
				  .append("\n\tsql config fn-name : ").append(name)
				  .append("\n\tsql config fn-args : ").append(args)
				  .append("\n\treplacement value  : ").append(replacement)
				  .append("\n\tafter replace value: ").append(after);
				//System.out.println(sb.toString());
				CommonLogger.debug(sb.toString());
			}
			context = after;
			matcher = sqlConfigFunctionPattern.matcher(context);
		}
		return context;
	}
	
	private static String[] resolveArgs(String arg){
		List<String> args = new ArrayList<String>();
		/**
		 * -2 : 处于#{的环境
		 * -1 : 处于${的环境
		 *  0 : 正常环境
		 * >0 : 处于(嵌套的环境，数值表示嵌套的重数
		 */
		int match = 0;
		int match2 = 0;//大括号{的重数
		char[] chs = arg.toCharArray();
		StringBuffer sb = new StringBuffer();
		for(int i = 0, l = chs.length; i < l; i++){
			char ch = chs[i];
			if(i > 0 && chs[i - 1] == '\\'){//转义字符，删除最后的\，并插入原字符
				sb.deleteCharAt(sb.length()-1);
				sb.append(ch);
				continue;
			}else if(match == 0 && ch == ','){//正常环境，并且为,，则结束当前参数的解析
				String v = sb.toString();
				if(!Tool.CHECK.isBlank(v)){
					args.add(v.trim());
				}
				sb.setLength(0);
				continue;
			}else{
				sb.append(ch);
			}
			
			switch(ch){
			case '#':
				if(0 == match && i < l-1 && chs[i+1] == '{'){//正常环境，并且下一字符为{，切换环境
					match = -2;
				}
				break;
			case '$':
				if(0 == match && i < l-1 && chs[i+1] == '{'){//正常环境，并且下一字符为{，切换环境
					match = -1;
				}
				break;
			case '{':
				if(match == -2 || match == -1){
					match2++;
				}
				break;
			case '}':
				if(match == -2 || match == -1){
					match2--;
					if(match2 == 0){
						match = 0;
					}
				}
				break;
			case '(':
				if(match >= 0){//正常环境，或已处于小括号环境，嵌套重数加1
					match++;
				}
				break;
			case ')':
				if(match > 0){//处于小括号环境，嵌套重数减1
					match--;
				}
				break;
			default:
				break;
			}
		}
		String v = sb.toString();
		if(!Tool.CHECK.isBlank(v)){
			args.add(v.trim());
		}
		
		return args.toArray(new String[args.size()]);
	}
	
	private static String resolveSqlConfigFunction(String databaseId, String name, String[] args){
		ISqlConfigFunction fn = SchemaHandlers.getSqlConfigFunction(name);
		if(null == fn){
			Throw.throwException(DaoExceptionCodes.DYSD020022, name);
		}
		String rs = fn.eval(databaseId, args);
		return rs == null ? "" : rs;
	}
}
