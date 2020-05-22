package fm.force.ui

import kotlinext.js.js
import kotlinext.js.Object
import kotlinext.js.assign
import org.w3c.dom.History
import kotlin.browser.window
import kotlin.reflect.KProperty1

interface Store<S, A, R> {
    fun getState(): S

    fun dispatch(action: A): R

    fun subscribe(listener: () -> Unit): () -> Unit

    fun replaceReducer(nextReducer: Reducer<S, A>)
}

interface ReducerContainer<S, A>

interface ActionCreatorContainer<A>

interface BoundActionCreatorContainer<A, R>

interface MiddlewareApi<S, A, R> {
    fun getState(): S

    fun dispatch(action: A): R
}

typealias Reducer<S, A> = (S, A) -> S

typealias StoreCreator<S, A, R> = (Reducer<S, A>, S) -> Store<S, A, R>

typealias Enhancer<S, A1, R1, A2, R2> = (StoreCreator<S, A1, R1>) -> StoreCreator<S, A2, R2>

typealias Middleware<S, A1, R1, A2, R2> = (MiddlewareApi<S, A1, R1>) -> ((A1) -> R1) -> (A2) -> R2

interface Action {
    val type: String
}

interface WrapperAction : Action {
    override val type: String
    val action: RAction
}

interface RAction


fun currentUserReducer(state: Long, action: RAction): Long = when (action) {
    else -> state
}

fun <S, A1, R1, A2, R2> routerMiddleware(history: History): Middleware<S, A1, R1, A2, R2> {
    return js("{}").unsafeCast<Middleware<S, A1, R1, A2, R2>>()
}


data class State(
   val id: Long = 1
)

fun <S, A> combineReducers(reducers: Map<String, Reducer<*, A>>): Reducer<S, A> = { s, a -> s }

fun <S, A, R> customCombineReducers(reducers: Map<KProperty1<S, R>, Reducer<*, A>>): Reducer<S, A> {
    return combineReducers(reducers.mapKeys { it.key.name })
}

fun combinedReducers(history: History) = customCombineReducers(
    mapOf(
        State::id to ::currentUserReducer
    )
)


fun <S> customEnhancer(): Enhancer<S, Action, Action, RAction, WrapperAction> = { next ->
    { reducer, initialState ->
        fun wrapperReducer(reducer: Reducer<S, RAction>): Reducer<S, WrapperAction> = { state, action ->
            if (!action.asDynamic().isKotlin as Boolean) {
                reducer(state, action.asDynamic().unsafeCast<RAction>())
            } else {
                reducer(state, action.action)
            }
        }

        val nextStoreCreator = next.unsafeCast<StoreCreator<S, WrapperAction, WrapperAction>>()
        val store = nextStoreCreator(
            wrapperReducer(reducer),
            Object.assign(js("{}"), initialState) as S
        )

        assign(Object.assign(js("{}"), store)) {
            dispatch = { action: dynamic ->
                // original redux actions use `type` keyword, so we don't reshape them
                if (action.type != undefined && action.action == undefined) {
                    store.dispatch(action.unsafeCast<WrapperAction>())
                } else {
                    // it's a Kotlin action, so we'll reshape it and provide a marker for the wrapper
                    store.dispatch(
                        js {
                            type = action::class.simpleName
                            isKotlin = true
                            this.action = action
                            Unit
                        }.unsafeCast<WrapperAction>()
                    )
                }
            }
            replaceReducer = { nextReducer: Reducer<S, RAction> ->
                store.replaceReducer(wrapperReducer(nextReducer))
            }
        }.unsafeCast<Store<S, RAction, WrapperAction>>()
    }
}
@Suppress("UnsafeCastFromDynamic")
fun <A, T1, R> composeWithDevTools(function1: (T1) -> R, function2: (A) -> T1): (A) -> R {
    return window.asDynamic().__REDUX_DEVTOOLS_EXTENSION_COMPOSE__(function1, function2)
}

fun <S, A, R> createStore(
    reducer: Reducer<S, A>,
    preloadedState: S,
    enhancer: Enhancer<S, Action, Action, A, R>
): Store<S, A, R> {
    throw NotImplementedError()
}

fun <S, A1, R1, A2, R2> applyMiddleware(vararg middlewares: Middleware<S, A1, R1, A2, R2>): Enhancer<S, A1, R1, A2, R2> {
    throw NotImplementedError()

}

class ReduxStore(
    val store: Store<State, RAction, WrapperAction>
) {
    companion object {
        fun of(
            state: State,
            history: History
        ): ReduxStore {
            val store = createStore<State, RAction, WrapperAction>(
                combinedReducers(history),
                state,
                composeWithDevTools(
                    applyMiddleware(
                        routerMiddleware(history)
//                        createThunkMiddleware(client) { action, exc -> ThunkError(action, exc) }
                    ),
                    customEnhancer()
                )
            )
            return ReduxStore(store)
        }
    }
}
