package com.werb.eventbuskotlin

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.werb.eventbus.EventBus
import com.werb.eventbus.Subscriber
import com.werb.eventbus.ThreadMode
import com.werb.eventbuskotlin.card.Card
import com.werb.eventbuskotlin.card.CardViewType
import com.werb.eventbuskotlin.meizhi.MeizhiViewType
import com.werb.library.MoreAdapter
import kotlinx.android.synthetic.main.my_fragment.*
import java.net.URL

/** Created by wanbo <werbhelius@gmail.com> on 2017/8/29. */

class MyFragment : Fragment() {

    private val adapter = MoreAdapter()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.my_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        EventBus.register(this)

        info.text = "还未登录，请点击按钮登录"
        adapter.register(CardViewType())
                .register(MeizhiViewType())
                .attachTo(list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.unRegister(this)
    }

    private fun buildData() {
        val list = mutableListOf<Card>()
        for (i in 1..30) {
            list.add(Card())
        }
        adapter.loadData(list)
    }

    @Subscriber
    private fun login(event: LoginEvent) {
        if (event.login) {
            info.text = "登录成功"
            info.visibility = View.GONE
            list.layoutManager = LinearLayoutManager(context)
            buildData()
        } else {
            adapter.removeAllData()
            info.visibility = View.VISIBLE
            info.text = "还未登录，请点击按钮登录"
        }
    }

    @Subscriber(tag = "delete")
    private fun delete(event: CardDeleteEvent) {
        val position = event.position
        if (position != -1) {
            adapter.removeData(position)
            Snackbar.make(fragment_layout, "Card $position delete by tag \" delete \"", 3000)
                    .setAction("ok", {})
                    .show()
        }
    }

    @Subscriber(tag = "not delete")
    private fun back(event: CardDeleteEvent) {
        Snackbar.make(fragment_layout, "Card not delete by tag \" not delete \"", 500)
                .setAction("ok", {})
                .show()
    }

    @Subscriber(mode = ThreadMode.BACKGROUND)
    private fun request(event: RequestMeizhiEvent){
        val data = URL("http://gank.io/api/data/%E7%A6%8F%E5%88%A9/10/1").readText()
        LoginDialogFragment.newInstance("request", data).show(fragmentManager, "LoginDialogFragment")
    }

    @Subscriber(tag = "request load data")
    private fun loadRequest(event: LoginEvent){
        list.layoutManager = GridLayoutManager(context, 2)
        event.meizhis?.let {
            info.text = "登录成功"
            info.visibility = View.GONE
            adapter.loadData(it.results)
        }
    }

}