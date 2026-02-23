import logging
import sys
from datetime import datetime
from zoneinfo import ZoneInfo

def setup_logging():
    shanghai_tz = ZoneInfo("Asia/Shanghai")

    formatter = logging.Formatter(
        '%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S'
    )
    
    def shanghai_converter(timestamp):
        return datetime.fromtimestamp(timestamp, tz=shanghai_tz).timetuple()
    
    formatter.converter = shanghai_converter

    handler = logging.StreamHandler(sys.stdout)
    handler.setFormatter(formatter)

    logging.basicConfig(
        level=logging.INFO,
        handlers=[handler]
    )
    
    return logging.getLogger("score_service")
