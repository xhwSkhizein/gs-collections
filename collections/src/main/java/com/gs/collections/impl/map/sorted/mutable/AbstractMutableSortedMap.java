/*
 * Copyright 2015 Goldman Sachs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gs.collections.impl.map.sorted.mutable;

import com.gs.collections.api.LazyIterable;
import com.gs.collections.api.block.function.Function;
import com.gs.collections.api.block.function.Function2;
import com.gs.collections.api.block.function.primitive.BooleanFunction;
import com.gs.collections.api.block.function.primitive.ByteFunction;
import com.gs.collections.api.block.function.primitive.CharFunction;
import com.gs.collections.api.block.function.primitive.DoubleFunction;
import com.gs.collections.api.block.function.primitive.FloatFunction;
import com.gs.collections.api.block.function.primitive.IntFunction;
import com.gs.collections.api.block.function.primitive.LongFunction;
import com.gs.collections.api.block.function.primitive.ShortFunction;
import com.gs.collections.api.block.predicate.Predicate;
import com.gs.collections.api.block.predicate.Predicate2;
import com.gs.collections.api.block.procedure.Procedure;
import com.gs.collections.api.block.procedure.primitive.ObjectIntProcedure;
import com.gs.collections.api.list.MutableList;
import com.gs.collections.api.list.primitive.MutableBooleanList;
import com.gs.collections.api.list.primitive.MutableByteList;
import com.gs.collections.api.list.primitive.MutableCharList;
import com.gs.collections.api.list.primitive.MutableDoubleList;
import com.gs.collections.api.list.primitive.MutableFloatList;
import com.gs.collections.api.list.primitive.MutableIntList;
import com.gs.collections.api.list.primitive.MutableLongList;
import com.gs.collections.api.list.primitive.MutableShortList;
import com.gs.collections.api.map.sorted.ImmutableSortedMap;
import com.gs.collections.api.map.sorted.MutableSortedMap;
import com.gs.collections.api.multimap.list.MutableListMultimap;
import com.gs.collections.api.multimap.sortedset.MutableSortedSetMultimap;
import com.gs.collections.api.ordered.OrderedIterable;
import com.gs.collections.api.partition.list.PartitionMutableList;
import com.gs.collections.api.stack.MutableStack;
import com.gs.collections.api.tuple.Pair;
import com.gs.collections.impl.block.factory.Functions;
import com.gs.collections.impl.block.factory.Predicates;
import com.gs.collections.impl.block.procedure.MapCollectProcedure;
import com.gs.collections.impl.block.procedure.PartitionPredicate2Procedure;
import com.gs.collections.impl.block.procedure.PartitionProcedure;
import com.gs.collections.impl.block.procedure.SelectInstancesOfProcedure;
import com.gs.collections.impl.block.procedure.primitive.CollectBooleanProcedure;
import com.gs.collections.impl.block.procedure.primitive.CollectByteProcedure;
import com.gs.collections.impl.block.procedure.primitive.CollectCharProcedure;
import com.gs.collections.impl.block.procedure.primitive.CollectDoubleProcedure;
import com.gs.collections.impl.block.procedure.primitive.CollectFloatProcedure;
import com.gs.collections.impl.block.procedure.primitive.CollectIntProcedure;
import com.gs.collections.impl.block.procedure.primitive.CollectLongProcedure;
import com.gs.collections.impl.block.procedure.primitive.CollectShortProcedure;
import com.gs.collections.impl.factory.SortedMaps;
import com.gs.collections.impl.list.fixed.ArrayAdapter;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.list.mutable.primitive.BooleanArrayList;
import com.gs.collections.impl.list.mutable.primitive.ByteArrayList;
import com.gs.collections.impl.list.mutable.primitive.CharArrayList;
import com.gs.collections.impl.list.mutable.primitive.DoubleArrayList;
import com.gs.collections.impl.list.mutable.primitive.FloatArrayList;
import com.gs.collections.impl.list.mutable.primitive.IntArrayList;
import com.gs.collections.impl.list.mutable.primitive.LongArrayList;
import com.gs.collections.impl.list.mutable.primitive.ShortArrayList;
import com.gs.collections.impl.map.mutable.AbstractMutableMapIterable;
import com.gs.collections.impl.multimap.list.FastListMultimap;
import com.gs.collections.impl.partition.list.PartitionFastList;
import com.gs.collections.impl.utility.Iterate;
import com.gs.collections.impl.utility.MapIterate;

public abstract class AbstractMutableSortedMap<K, V> extends AbstractMutableMapIterable<K, V>
        implements MutableSortedMap<K, V>
{
    @SuppressWarnings("AbstractMethodOverridesAbstractMethod")
    public abstract MutableSortedMap<K, V> clone();

    public MutableSortedMap<K, V> withKeyValue(K key, V value)
    {
        this.put(key, value);
        return this;
    }

    public MutableSortedMap<K, V> withAllKeyValues(Iterable<? extends Pair<? extends K, ? extends V>> keyValues)
    {
        for (Pair<? extends K, ? extends V> keyVal : keyValues)
        {
            this.put(keyVal.getOne(), keyVal.getTwo());
        }
        return this;
    }

    /**
     * @deprecated in 6.0 Use {@link #withAllKeyValueArguments(Pair[])} instead. Inlineable.
     */
    @Deprecated
    public MutableSortedMap<K, V> with(Pair<K, V>... pairs)
    {
        return this.withAllKeyValueArguments(pairs);
    }

    public MutableSortedMap<K, V> withAllKeyValueArguments(Pair<? extends K, ? extends V>... keyValues)
    {
        return this.withAllKeyValues(ArrayAdapter.adapt(keyValues));
    }

    public MutableSortedMap<K, V> withoutKey(K key)
    {
        this.removeKey(key);
        return this;
    }

    public MutableSortedMap<K, V> withoutAllKeys(Iterable<? extends K> keys)
    {
        for (K key : keys)
        {
            this.removeKey(key);
        }
        return this;
    }

    public MutableSortedMap<K, V> asUnmodifiable()
    {
        return UnmodifiableTreeMap.of(this);
    }

    public ImmutableSortedMap<K, V> toImmutable()
    {
        return SortedMaps.immutable.withSortedMap(this);
    }

    public MutableSortedMap<K, V> asSynchronized()
    {
        return SynchronizedSortedMap.of(this);
    }

    public MutableSortedSetMultimap<V, K> flip()
    {
        return MapIterate.flip(this);
    }

    public MutableBooleanList collectBoolean(BooleanFunction<? super V> booleanFunction)
    {
        BooleanArrayList result = new BooleanArrayList(this.size());
        this.forEach(new CollectBooleanProcedure<V>(booleanFunction, result));
        return result;
    }

    public MutableByteList collectByte(ByteFunction<? super V> byteFunction)
    {
        ByteArrayList result = new ByteArrayList(this.size());
        this.forEach(new CollectByteProcedure<V>(byteFunction, result));
        return result;
    }

    public MutableCharList collectChar(CharFunction<? super V> charFunction)
    {
        CharArrayList result = new CharArrayList(this.size());
        this.forEach(new CollectCharProcedure<V>(charFunction, result));
        return result;
    }

    public MutableDoubleList collectDouble(DoubleFunction<? super V> doubleFunction)
    {
        DoubleArrayList result = new DoubleArrayList(this.size());
        this.forEach(new CollectDoubleProcedure<V>(doubleFunction, result));
        return result;
    }

    public MutableFloatList collectFloat(FloatFunction<? super V> floatFunction)
    {
        FloatArrayList result = new FloatArrayList(this.size());
        this.forEach(new CollectFloatProcedure<V>(floatFunction, result));
        return result;
    }

    public MutableIntList collectInt(IntFunction<? super V> intFunction)
    {
        IntArrayList result = new IntArrayList(this.size());
        this.forEach(new CollectIntProcedure<V>(intFunction, result));
        return result;
    }

    public MutableLongList collectLong(LongFunction<? super V> longFunction)
    {
        LongArrayList result = new LongArrayList(this.size());
        this.forEach(new CollectLongProcedure<V>(longFunction, result));
        return result;
    }

    public MutableShortList collectShort(ShortFunction<? super V> shortFunction)
    {
        ShortArrayList result = new ShortArrayList(this.size());
        this.forEach(new CollectShortProcedure<V>(shortFunction, result));
        return result;
    }

    public <E> MutableSortedMap<K, V> collectKeysAndValues(
            Iterable<E> iterable,
            Function<? super E, ? extends K> keyFunction,
            Function<? super E, ? extends V> valueFunction)
    {
        Iterate.forEach(iterable, new MapCollectProcedure<E, K, V>(this, keyFunction, valueFunction));
        return this;
    }

    public <R> MutableSortedMap<K, R> collectValues(Function2<? super K, ? super V, ? extends R> function)
    {
        return MapIterate.collectValues(this, function, TreeSortedMap.<K, R>newMap(this.comparator()));
    }

    public MutableSortedMap<K, V> tap(Procedure<? super V> procedure)
    {
        this.forEach(procedure);
        return this;
    }

    public MutableSortedMap<K, V> select(Predicate2<? super K, ? super V> predicate)
    {
        return MapIterate.selectMapOnEntry(this, predicate, this.newEmpty());
    }

    public MutableSortedMap<K, V> reject(Predicate2<? super K, ? super V> predicate)
    {
        return MapIterate.rejectMapOnEntry(this, predicate, this.newEmpty());
    }

    public <R> MutableList<R> collect(Function<? super V, ? extends R> function)
    {
        return this.collect(function, FastList.<R>newList(this.size()));
    }

    public <P, VV> MutableList<VV> collectWith(Function2<? super V, ? super P, ? extends VV> function, P parameter)
    {
        return this.collect(Functions.bind(function, parameter));
    }

    public <R> MutableList<R> collectIf(Predicate<? super V> predicate, Function<? super V, ? extends R> function)
    {
        return this.collectIf(predicate, function, FastList.<R>newList(this.size()));
    }

    public <R> MutableList<R> flatCollect(Function<? super V, ? extends Iterable<R>> function)
    {
        return this.flatCollect(function, FastList.<R>newList(this.size()));
    }

    public MutableList<V> reject(Predicate<? super V> predicate)
    {
        return this.reject(predicate, FastList.<V>newList(this.size()));
    }

    public <P> MutableList<V> selectWith(Predicate2<? super V, ? super P> predicate, P parameter)
    {
        return this.select(Predicates.bind(predicate, parameter));
    }

    public <P> MutableList<V> rejectWith(Predicate2<? super V, ? super P> predicate, P parameter)
    {
        return this.reject(Predicates.bind(predicate, parameter));
    }

    public MutableList<V> select(Predicate<? super V> predicate)
    {
        return this.select(predicate, FastList.<V>newList(this.size()));
    }

    public PartitionMutableList<V> partition(Predicate<? super V> predicate)
    {
        PartitionMutableList<V> partitionMutableList = new PartitionFastList<V>();
        this.forEach(new PartitionProcedure<V>(predicate, partitionMutableList));
        return partitionMutableList;
    }

    public <P> PartitionMutableList<V> partitionWith(Predicate2<? super V, ? super P> predicate, P parameter)
    {
        PartitionMutableList<V> partitionMutableList = new PartitionFastList<V>();
        this.forEach(new PartitionPredicate2Procedure<V, P>(predicate, parameter, partitionMutableList));
        return partitionMutableList;
    }

    public <S> MutableList<S> selectInstancesOf(Class<S> clazz)
    {
        FastList<S> result = FastList.newList(this.size());
        this.forEach(new SelectInstancesOfProcedure<S>(clazz, result));
        result.trimToSize();
        return result;
    }

    public <S> MutableList<Pair<V, S>> zip(Iterable<S> that)
    {
        return this.zip(that, FastList.<Pair<V, S>>newList(this.size()));
    }

    public MutableList<Pair<V, Integer>> zipWithIndex()
    {
        return this.zipWithIndex(FastList.<Pair<V, Integer>>newList(this.size()));
    }

    public <VV> MutableListMultimap<VV, V> groupBy(Function<? super V, ? extends VV> function)
    {
        return this.groupBy(function, FastListMultimap.<VV, V>newMultimap());
    }

    public <VV> MutableListMultimap<VV, V> groupByEach(Function<? super V, ? extends Iterable<VV>> function)
    {
        return this.groupByEach(function, FastListMultimap.<VV, V>newMultimap());
    }

    public void reverseForEach(Procedure<? super V> procedure)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + ".reverseForEach() not implemented yet");
    }

    public LazyIterable<V> asReversed()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + ".asReversed() not implemented yet");
    }

    public int detectLastIndex(Predicate<? super V> predicate)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + ".detectLastIndex() not implemented yet");
    }

    public int indexOf(Object object)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + ".indexOf() not implemented yet");
    }

    public <S> boolean corresponds(OrderedIterable<S> other, Predicate2<? super V, ? super S> predicate)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + ".corresponds() not implemented yet");
    }

    public void forEach(int startIndex, int endIndex, Procedure<? super V> procedure)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + ".forEach() not implemented yet");
    }

    public void forEachWithIndex(int fromIndex, int toIndex, ObjectIntProcedure<? super V> objectIntProcedure)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + ".forEachWithIndex() not implemented yet");
    }

    public MutableStack<V> toStack()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + ".toStack() not implemented yet");
    }

    public int detectIndex(Predicate<? super V> predicate)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + ".detectIndex() not implemented yet");
    }
}
