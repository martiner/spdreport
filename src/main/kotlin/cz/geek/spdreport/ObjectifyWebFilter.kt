package cz.geek.spdreport

import com.googlecode.objectify.ObjectifyService
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse

class ObjectifyWebFilter : Filter {

    override fun doFilter(req: ServletRequest, resp: ServletResponse, chain: FilterChain) =
        ObjectifyService.begin()
            .use {
                chain.doFilter(req, resp)
            }
}
