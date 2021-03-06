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
package org.dysd.dao.mybatis.component;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLStatementBuilder;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.xmltags.XMLScriptBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.dysd.dao.mybatis.mapper.IParamResolver;

/**
 * Mybatis组件工厂
 * @author linjisong
 * @version 0.0.1
 * @date 2016-11-12
 */
public interface MybatisComponentFactory {

	/**
	 * 创建SqlSessionFactoryBuilder对象
	 * @return
	 */
	public SqlSessionFactoryBuilder newSqlSessionFactoryBuilder();
	
	/**
	 * 创建mybatis全局配置解析器
	 * @param inputStream
	 * @param environment
	 * @param props
	 * @return
	 */
	public XMLConfigBuilder newXMLConfigBuilder(InputStream inputStream, String environment, Properties props);
	
	/**
	 * 创建SqlMapper解析器
	 * @param inputStream
	 * @param configuration
	 * @param resource
	 * @param sqlFragments
	 * @return
	 */
	public XMLMapperBuilder newXMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments);
	
	/**
	 * 创建语句级元素解析器
	 * @param configuration
	 * @param builder
	 * @param node
	 * @param requiredDatabaseId
	 * @return
	 */
	public XMLStatementBuilder newXMLStatementBuilder(Configuration configuration, MapperBuilderAssistant builder, XNode node, String requiredDatabaseId);
	
	/**
	 * 创建脚本级元素解析器
	 * @param configuration
	 * @param context
	 * @param parameterType
	 * @return
	 */
	public XMLScriptBuilder newXMLScriptBuilder(Configuration configuration, XNode context, Class<?> parameterType);
	
	/**
	 * 创建配置对象
	 * @return
	 */
	public Configuration newConfiguration();
	
	/**
	 * 创建mybatis环境
	 * @param id
	 * @param transactionFactory
	 * @param dataSource
	 * @return
	 */
	public Environment newEnvironment(String id, TransactionFactory transactionFactory, DataSource dataSource);
	
	/**
	 * 创建mybatis事务工厂
	 * @return
	 */
	public TransactionFactory newTransactionFactory();
	
	/**
	 * 创建SqlSourceBuilder对象
	 * @param configuration
	 * @return
	 */
	public SqlSourceBuilder newSqlSourceBuilder(Configuration configuration);
	
	/**
	 * 创建参数处理器
	 * @param mappedStatement
	 * @param parameterObject
	 * @param boundSql
	 * @return
	 */
	public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);

	/**
	 * 创建自动代理时的参数解析器
	 * @param config      mybatis配置
	 * @param method      dao接口方法
	 * @param isBatch     是否批量
	 * @return
	 */
	public IParamResolver newParamResolver(Configuration config, Method method, boolean isBatch);
}
