package mv.haluvi.webconnector
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply { text = "Welcome to your accessible Android project"; textSize = 24f; contentDescription = text })
    }
}
