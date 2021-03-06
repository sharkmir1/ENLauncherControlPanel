package kr.ac.snu.hcil.datahalo.utils

class MapFunctionUtilities {
    companion object Tools {
        fun bin(range: Pair<Double, Double>, numOfBin: Int)
                :List<Pair<Double, Double>>{
            val binSize = (range.second - range.first) / numOfBin
            return List(numOfBin){index -> Pair(range.first + index * binSize , range.first + (index + 1) * binSize)}
        }

        fun createMapFunc(xRange: Pair<Double, Double>, yRange: Pair<Double, Double>): (Any) -> (Double?){
            return {x: Any ->
                if (x is Double) {
                    val xRangeSize = xRange.second - xRange.first
                    val yRangeSize = yRange.second - yRange.first

                    if(xRangeSize == 0.0)
                        null
                    else{
                        val input: Double =
                                when{
                                    xRangeSize > 0.0 -> when{
                                        x < xRange.first -> xRange.first
                                        x > xRange.second -> xRange.second
                                        else -> x }
                                    xRangeSize < 0.0 -> when{
                                        x > xRange.first -> xRange.first
                                        x < xRange.second -> xRange.second
                                        else -> x }
                                    else -> x
                                }
                        (yRangeSize / xRangeSize) * (input - xRange.first) + yRange.first
                    }
                }
                else {null}
            }
        }

        fun createMapFunc(threshold: Int, yRange: Pair<Double, Double>):(Any) -> (Double?){
            return {x: Any ->
                if (x is Int){
                    val yRangeSize = yRange.second - yRange.first
                    val slope = yRangeSize / threshold
                    if(x >= threshold) {slope * threshold + yRange.first}
                    else{slope * x + yRange.first}
                }
                else null
            }
        }

        fun<T> createMapFunc(thresholds: List<Int>, valRange: List<T>, numOfBin: Int = valRange.size): (Any) -> (T?){
            return {x:Any ->
                if(x is Int){
                    val index = thresholds.indexOfFirst {ref -> ref >= x }
                    if(index > 0)
                        valRange[index]
                    else
                        null
                }
                else{
                    null
                }
            }
        }

        fun<T> createMapFunc(keyRange: Pair<Double, Double>, valRange: List<T>, numOfBin: Int = valRange.size): (Any) -> (T?){
            val newKeyList = bin(keyRange, numOfBin)

            return {x:Any ->
                if(x is Double)
                {
                    val index = newKeyList.indexOfFirst{
                        if(it.second >= it.first)
                            (x >= it.first) && (x < it.second)
                        else
                            (x <= it.first) && (x > it.second)
                    }
                    valRange[index]
                }
                else{
                    null
                }
            }
        }

        fun<T> createBinnedNumericRangeMapFunc(keyRangeList: List<Pair<Double, Double>>, valRange: List<T>): (Any) -> (T?){
            return { x: Any ->
                if(x is Double)
                {
                    val index = keyRangeList.indexOfFirst{
                        if(it.second >= it.first)
                            (x >= it.first) && (x < it.second)
                        else
                            (x <= it.first) && (x > it.second)
                    }
                    valRange[index]
                }
                else{
                    null
                }
            }
        }

        //TODO(충분히 null 나올 수 있음. 데이터 매핑에서 사용자가 선택하지 않은 property value는 null처리 해줘야 함

        fun<T> createMapFunc(keyRange: List<T>, valRange:Pair<Double, Double>, numOfBin: Int = keyRange.size): (T) -> Double? {
            val newValList = bin(valRange, numOfBin) // [(0, 0.2), (0.2, 0.4), (0.4, 0.6)]

            return { key: T ->
                val index = keyRange.indexOf(key)
                if (index == -1) {
                    null

                } else {
                    val pair = newValList[index]
                    (pair.first + pair.second) / 2
                }
            }
        }

        fun<T> createMapFuncToBinnedNumericRange(keyRange: List<T>, valRange:List<Pair<Double, Double>>):(T) -> Double? {
            return { key: T ->
                val index = keyRange.indexOf(key)
                if (index == -1) {
                    null

                } else {
                    val pair = valRange[index]
                    (pair.first + pair.second) / 2
                }
            }
        }


        fun<T, R> createMapFunc(keyRange: List<T>, valRange: List<R>): (T) -> (R?){
            return { key -> if(key in keyRange) valRange[keyRange.indexOf(key)] else null}
        }
    }
}