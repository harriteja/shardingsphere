/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.optimize.sharding.engnie.dml;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingConditionOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableAvailable;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingUpdateOptimizeEngineTest {

    private ShardingRule shardingRule;

    private TableMetas tableMetas;

    private String sql;

    private List<Object> parameters;

    private UpdateStatement sqlStatement;

    @Before
    public void setUp() {
        sql = "update user set id = 100";
        shardingRule = mock(ShardingRule.class);
        tableMetas = mock(TableMetas.class);
        parameters = Collections.emptyList();
        sqlStatement = mock(UpdateStatement.class);
        TableAvailable tableAvailable = mock(TableAvailable.class);
        AssignmentSegment assignmentSegment = mock(AssignmentSegment.class);
        ColumnSegment columnSegment = mock(ColumnSegment.class);
        when(columnSegment.getName()).thenReturn("id");
        when(assignmentSegment.getColumn()).thenReturn(columnSegment);
        when(tableAvailable.getTableName()).thenReturn("user");
        when(sqlStatement.findSQLSegments(TableAvailable.class)).thenReturn(Arrays.asList(tableAvailable));
        SetAssignmentsSegment setAssignmentsSegment = mock(SetAssignmentsSegment.class);
        when(setAssignmentsSegment.getAssignments()).thenReturn(Arrays.asList(assignmentSegment));
        when(sqlStatement.getSetAssignment()).thenReturn(setAssignmentsSegment);
        when(sqlStatement.getWhere()).thenReturn(Optional.<WhereSegment>absent());
    }

    @Test
    public void assertUpdateWithoutShardKey() {
        when(shardingRule.isShardingColumn("id", "user")).thenReturn(false);
        ShardingUpdateOptimizeEngine shardingUpdateOptimizeEngine = new ShardingUpdateOptimizeEngine();
        ShardingConditionOptimizedStatement optimizedStatement = shardingUpdateOptimizeEngine.optimize(shardingRule, tableMetas, sql, parameters, sqlStatement);
        assertThat(optimizedStatement, instanceOf(ShardingConditionOptimizedStatement.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void assertUpdateWithShardKey() {
        when(shardingRule.isShardingColumn("id", "user")).thenReturn(true);
        ShardingUpdateOptimizeEngine shardingUpdateOptimizeEngine = new ShardingUpdateOptimizeEngine();
        shardingUpdateOptimizeEngine.optimize(shardingRule, tableMetas, sql, parameters, sqlStatement);
    }
}
