/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.dex.base;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;

import java.io.IOException;

public class CountedList<T extends Block> extends BlockList<T> {

    private final IntegerReference itemCount;

    public CountedList(IntegerReference itemCount, Creator<T> creator){
        super(creator);
        this.itemCount = itemCount;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        setSize(itemCount.get());
        super.readChildes(reader);
    }

    @Override
    public void setSize(int size) {
        super.setSize(size);
        if(size != itemCount.get()){
            itemCount.set(size);
        }
    }
    @Override
    protected void onRefreshed() {
        itemCount.set(size());
    }
}
